package dev.casteels.plukk.shopping.history;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
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
import dev.casteels.plukk.catalog.api.SearchCatalogProductsUseCase;
import dev.casteels.plukk.shopping.input.AddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.CreateCustomProductAndAddShoppingNeedUseCase;
import dev.casteels.plukk.shopping.input.ShoppingInputParser;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import dev.casteels.plukk.shopping.item.PurchaseShoppingItemUseCase;
import dev.casteels.plukk.shopping.list.CreateShoppingListUseCase;
import dev.casteels.plukk.shopping.list.ShoppingList;

@SpringBootTest(classes = PlukkApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ShoppingHistoryIntegrationTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired private JdbcClient jdbc;
    @Autowired private CreateShoppingListUseCase createList;
    @Autowired private AddShoppingNeedUseCase addNeed;
    @Autowired private CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct;
    @Autowired private PurchaseShoppingItemUseCase purchaseItem;
    @Autowired private SearchCatalogProductsUseCase searchCatalog;
    @Autowired private ListRecentShoppingNeedsUseCase listRecentNeeds;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    private void authenticateAs(String subject) {
        jdbc.sql("DELETE FROM household_member WHERE external_subject = :subject").param("subject", subject).update();
        jdbc.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, :subject, :subject, 'MEMBER')")
                .param("subject", subject).update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(subject, "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        jdbc.sql("DELETE FROM shopping_list").update();
        jdbc.sql("DELETE FROM shopping_history_entry WHERE household_id = 1").update();
        jdbc.sql("DELETE FROM catalog_product WHERE household_id = 1 AND origin = 'CUSTOM'").update();
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenCustomProductCreatedByOneMember_whenAnotherMemberSearchesCatalog_thenProductIsVisible() {
        authenticateAs("history-persistence-creator");
        ShoppingList list = createList.execute("Groceries").list();
        long categoryId = jdbc.sql("SELECT id FROM category WHERE household_id = 1 ORDER BY display_order LIMIT 1").query(Long.class).single();
        ShoppingInputParser.InterpretedNeed need = new ShoppingInputParser.InterpretedNeed("Tofu", null, new BigDecimal("2"), null, null, null, null);
        createCustomProduct.execute(list.id(), need, categoryId);

        authenticateAs("history-persistence-searcher");
        SearchCatalogProductsUseCase.Result result = searchCatalog.execute("tofu");

        assertThat(result.matches()).extracting("name").containsExactly("Tofu");
    }

    @Test
    void givenItemPurchasedByOneMember_whenAnotherMemberListsRecentNeeds_thenEntryIsHouseholdWideVisible() {
        authenticateAs("history-persistence-buyer");
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed added = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kipfilet 400g");
        purchaseItem.execute(list.id(), added.item().id());

        authenticateAs("history-persistence-viewer");
        ListRecentShoppingNeedsUseCase.Result result = listRecentNeeds.execute();

        assertThat(result.needs()).extracting("variant").contains("Kipfilet");
    }

    @Test
    void givenRepeatedPurchaseOfSameConcreteNeed_whenRecordingHistory_thenOnlyOneEntryIsRetained() {
        authenticateAs("history-persistence-repeat");
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed first = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kipfilet 400g");
        purchaseItem.execute(list.id(), first.item().id());

        ShoppingNeedOutcome.Confirmed second = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "kipfilet 400g");
        purchaseItem.execute(list.id(), second.item().id());

        int entryCount = jdbc.sql("""
                        SELECT COUNT(*) FROM shopping_history_entry
                        WHERE household_id = 1 AND variant = 'Kipfilet' AND quantity = 400 AND unit = 'gram'
                        """).query(Integer.class).single();
        assertThat(entryCount).isEqualTo(1);
    }
}
