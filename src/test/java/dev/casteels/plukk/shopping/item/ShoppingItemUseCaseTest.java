package dev.casteels.plukk.shopping.item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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

import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import dev.casteels.plukk.shopping.list.CreateShoppingListUseCase;
import dev.casteels.plukk.shopping.list.ShoppingList;

@SpringBootTest(classes = PlukkApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ShoppingItemUseCaseTest {
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
    void givenActiveMember_whenItemBehaviorRuns_thenAuthenticateMember() {
        jdbc.sql("DELETE FROM shopping_list").update();
        jdbc.sql("DELETE FROM household_member WHERE external_subject = 'item-member'").update();
        jdbc.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'item-member', 'Item Member', 'MEMBER')").update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("item-member", "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenActiveItemsInDifferentCategories_whenGettingSections_thenItemsAreGrouped() {
        ShoppingList list = createList.execute("Groceries").list();
        addNeed.execute(list.id(), "kip 1");
        addNeed.execute(list.id(), "melk 2x1l");

        GetShoppingListSectionsUseCase.Result result = getSections.execute(list.id());

        assertThat(result.notification().isSuccess()).isTrue();
        assertThat(result.sections()).extracting(ShoppingListSection::categoryName).contains("Meat", "Dairy");
    }

    @Test
    void givenActiveItem_whenMarkingPurchased_thenItemRemainsVisibleAsPurchased() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kip 1");

        PurchaseShoppingItemUseCase.Result result = purchaseItem.execute(list.id(), added.item().id());

        assertThat(result.notification().isSuccess()).isTrue();
        assertThat(result.item().state()).isEqualTo(ShoppingItem.State.PURCHASED);
        List<ShoppingListSection> sections = getSections.execute(list.id()).sections();
        assertThat(sections.getFirst().items()).extracting(entry -> entry.item().state())
                .containsExactly(ShoppingItem.State.PURCHASED);
    }

    @Test
    void givenPurchasedItem_whenRestoring_thenItemReturnsToActive() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kip 1");
        purchaseItem.execute(list.id(), added.item().id());

        RestoreShoppingItemUseCase.Result result = restoreItem.execute(list.id(), added.item().id());

        assertThat(result.notification().isSuccess()).isTrue();
        assertThat(result.item().state()).isEqualTo(ShoppingItem.State.ACTIVE);
    }

    @Test
    void givenActiveItem_whenRemoving_thenItemNoLongerAppearsInSections() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kip 1");

        var notification = removeItem.execute(list.id(), added.item().id());

        assertThat(notification.isSuccess()).isTrue();
        assertThat(getSections.execute(list.id()).sections()).isEmpty();
    }

    @Test
    void givenUnknownItem_whenPurchasing_thenNotificationExplainsNotFound() {
        ShoppingList list = createList.execute("Groceries").list();

        PurchaseShoppingItemUseCase.Result result = purchaseItem.execute(list.id(), 999999L);

        assertThat(result.item()).isNull();
        assertThat(result.notification().issues()).extracting("code", "message")
                .containsExactly(tuple("shopping-item.not-found", "Shopping item not found."));
    }
}
