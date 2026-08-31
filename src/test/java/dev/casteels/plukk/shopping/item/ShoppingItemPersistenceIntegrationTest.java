package dev.casteels.plukk.shopping.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
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
class ShoppingItemPersistenceIntegrationTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired private JdbcClient jdbc;
    @Autowired private CreateShoppingListUseCase createList;
    @Autowired private AddShoppingNeedUseCase addNeed;
    @Autowired private GetShoppingListSectionsUseCase getSections;
    @Autowired private PurchaseShoppingItemUseCase purchaseItem;
    @Autowired private RestoreShoppingItemUseCase restoreItem;
    @Autowired private RemoveShoppingItemUseCase removeItem;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @BeforeEach
    void givenActiveMember_whenItemPersistenceRuns_thenAuthenticateMember() {
        jdbc.sql("DELETE FROM shopping_list").update();
        jdbc.sql("DELETE FROM household_member WHERE external_subject = 'item-persistence-member'").update();
        jdbc.sql("DELETE FROM household WHERE id = 2").update();
        jdbc.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'item-persistence-member', 'Item Persistence Member', 'MEMBER')").update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("item-persistence-member", "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenItemsInDisplayOrderedCategories_whenGettingSections_thenSectionsFollowCategoryDisplayOrder() {
        ShoppingList list = createList.execute("Groceries").list();
        addNeed.execute(list.id(), "kaas 1");
        addNeed.execute(list.id(), "appels 1");

        var sections = getSections.execute(list.id()).sections();

        assertThat(sections).extracting(ShoppingListSection::categoryName).containsExactly("Produce", "Pantry");
    }

    @Test
    void givenActiveDuplicateExists_whenRestoringAPurchasedItem_thenNotificationExplainsConflict() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed original = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kip 1");
        purchaseItem.execute(list.id(), original.item().id());
        addNeed.execute(list.id(), "kip 1");

        RestoreShoppingItemUseCase.Result result = restoreItem.execute(list.id(), original.item().id());

        assertThat(result.item()).isNull();
        assertThat(result.notification().issues()).extracting("code", "message")
                .containsExactly(tuple("shopping-item.duplicate", "An identical item is already active on this list."));
    }

    @Test
    void givenItemOnAnotherHouseholdsList_whenPurchasing_thenNotificationTreatsListAsNotFound() {
        jdbc.sql("INSERT INTO household (id, display_name) VALUES (2, 'Other household')").update();
        long otherListId = jdbc.sql("INSERT INTO shopping_list (household_id, name) VALUES (2, 'Private') RETURNING id")
                .query(Long.class).single();

        PurchaseShoppingItemUseCase.Result result = purchaseItem.execute(otherListId, 1L);

        assertThat(result.item()).isNull();
        assertThat(result.notification().issues()).extracting("code", "message")
                .containsExactly(tuple("shopping-list.not-found", "Shopping list not found."));
    }

    @Test
    void givenRemovedItem_whenRemovingAgain_thenNotificationExplainsNotFound() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kip 1");
        removeItem.execute(list.id(), added.item().id());

        var notification = removeItem.execute(list.id(), added.item().id());

        assertThat(notification.issues()).extracting("code", "message")
                .containsExactly(tuple("shopping-item.not-found", "Shopping item not found."));
    }
}
