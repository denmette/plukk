package dev.casteels.plukk.shopping.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.catalog.api.SearchCatalogProductsUseCase;
import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import dev.casteels.plukk.shopping.item.PurchaseShoppingItemUseCase;
import dev.casteels.plukk.shopping.list.CreateShoppingListUseCase;
import dev.casteels.plukk.shopping.list.ShoppingList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = PlukkApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ShoppingHistoryUseCaseTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired private JdbcClient jdbc;
    @Autowired private CreateShoppingListUseCase createList;
    @Autowired private AddShoppingNeedUseCase addNeed;
    @Autowired private PurchaseShoppingItemUseCase purchaseItem;
    @Autowired private SearchCatalogProductsUseCase searchCatalog;
    @Autowired private ListRecentShoppingNeedsUseCase listRecentNeeds;
    @Autowired private ReAddShoppingNeedUseCase reAddNeed;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @BeforeEach
    void givenActiveMember_whenHistoryBehaviorRuns_thenAuthenticateMember() {
        jdbc.sql("DELETE FROM shopping_list").update();
        jdbc.sql("DELETE FROM shopping_history_entry WHERE household_id = 1").update();
        jdbc.sql("DELETE FROM catalog_product WHERE household_id = 1 AND origin = 'CUSTOM'").update();
        jdbc.sql("DELETE FROM household_member WHERE external_subject = 'history-member'").update();
        jdbc.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'history-member', 'History Member', 'MEMBER')").update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("history-member", "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenCatalogContainsCommonProducts_whenSearchingByName_thenMatchesShowCategoryAndVisualAid() {
        SearchCatalogProductsUseCase.Result result = searchCatalog.execute("kip");

        assertThat(result.notification().isSuccess()).isTrue();
        assertThat(result.matches()).extracting("name", "categoryName", "visualReference")
                .containsExactly(tuple("Kip", "Meat", "kip"));
    }

    @Test
    void givenPreviouslyPurchasedConcreteNeed_whenReAddingFromRecentNeeds_thenNewActiveItemPreservesDetails() {
        ShoppingList original = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(original.id(), "kipfilet 400g");
        purchaseItem.execute(original.id(), added.item().id());

        ListRecentShoppingNeedsUseCase.Result recent = listRecentNeeds.execute();
        assertThat(recent.needs()).extracting("variant", "quantity", "unit")
                .containsExactly(tuple("Kipfilet", added.item().quantity(), "gram"));
        long entryId = recent.needs().getFirst().entryId();

        ShoppingList otherList = createList.execute("Weekend BBQ").list();
        ShoppingNeedOutcome result = reAddNeed.execute(otherList.id(), entryId);

        assertThat(result).isInstanceOfSatisfying(ShoppingNeedOutcome.Confirmed.class, confirmed -> {
            assertThat(confirmed.item().variant()).isEqualTo("Kipfilet");
            assertThat(confirmed.item().quantity()).isEqualByComparingTo(added.item().quantity());
            assertThat(confirmed.item().unit()).isEqualTo("gram");
        });
    }

    @Test
    void givenExactActiveItemAlreadyExists_whenReAddingFromRecentNeeds_thenNotificationReturnsExistingItem() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kipfilet 400g");
        purchaseItem.execute(list.id(), added.item().id());
        long entryId = listRecentNeeds.execute().needs().getFirst().entryId();
        ShoppingNeedOutcome.Confirmed existing = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kipfilet 400g");

        ShoppingNeedOutcome result = reAddNeed.execute(list.id(), entryId);

        assertThat(result).isInstanceOfSatisfying(ShoppingNeedOutcome.Duplicate.class, duplicate -> {
            assertThat(duplicate.itemId()).isEqualTo(existing.item().id());
            assertThat(duplicate.notification().issues()).extracting("code", "message")
                    .containsExactly(tuple("shopping-need.duplicate", "This item is already on the list."));
        });
    }

    @Test
    void givenUnknownHistoryEntry_whenReAdding_thenNotificationExplainsNotFound() {
        ShoppingList list = createList.execute("Groceries").list();

        ShoppingNeedOutcome result = reAddNeed.execute(list.id(), 999999L);

        assertThat(result).isInstanceOfSatisfying(ShoppingNeedOutcome.Rejected.class, rejected ->
                assertThat(rejected.notification().issues()).extracting("code", "message")
                        .containsExactly(tuple("shopping-history.not-found", "Recent need not found.")));
    }
}
