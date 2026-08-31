package dev.casteels.plukk.shopping.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.shopping.list.CreateShoppingListUseCase;
import dev.casteels.plukk.shopping.list.ShoppingList;
import java.math.BigDecimal;
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
class AddShoppingNeedIntegrationTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired private JdbcClient jdbc;
    @Autowired private CreateShoppingListUseCase createList;
    @Autowired private AddShoppingNeedUseCase addNeed;
    @Autowired private CreateCustomProductAndAddShoppingNeedUseCase createCustomProduct;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @BeforeEach
    void givenActiveMember_whenAddingNeeds_thenAuthenticateMember() {
        jdbc.sql("DELETE FROM shopping_list").update();
        jdbc.sql("DELETE FROM household_member WHERE external_subject = 'need-member'").update();
        jdbc.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'need-member', 'Need Member', 'MEMBER')").update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("need-member", "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenSeededCatalogProduct_whenExecutingAddNeedUseCase_thenConfirmedItemIsPersisted() {
        ShoppingList list = createList.execute("Groceries").list();

        ShoppingNeedOutcome result = addNeed.execute(list.id(), "kipfilet 400g");

        assertThat(result).isInstanceOfSatisfying(ShoppingNeedOutcome.Confirmed.class, confirmed -> {
            assertThat(confirmed.notification().isSuccess()).isTrue();
            assertThat(confirmed.item().variant()).isEqualTo("Kipfilet");
            assertThat(confirmed.item().quantity()).isEqualByComparingTo(new BigDecimal("400"));
            assertThat(confirmed.item().unit()).isEqualTo("gram");
        });
    }

    @Test
    void givenExactActiveNeed_whenExecutingAddNeedUseCase_thenNotificationReturnsExistingItem() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingNeedOutcome.Confirmed first = (ShoppingNeedOutcome.Confirmed) addNeed.execute(list.id(), "melk 2x1l");

        ShoppingNeedOutcome result = addNeed.execute(list.id(), "melk 2x1l");

        assertThat(result).isInstanceOfSatisfying(ShoppingNeedOutcome.Duplicate.class, duplicate -> {
            assertThat(duplicate.itemId()).isEqualTo(first.item().id());
            assertThat(duplicate.notification().issues()).extracting("code", "message")
                    .containsExactly(tuple("shopping-need.duplicate", "This item is already on the list."));
        });
        assertThat(jdbc.sql("SELECT COUNT(*) FROM shopping_item WHERE shopping_list_id = :listId").param("listId", list.id()).query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void givenMissingCatalogProduct_whenExecutingCustomProductUseCase_thenHouseholdProductAndItemArePersisted() {
        ShoppingList list = createList.execute("Groceries").list();
        ShoppingInputParser.InterpretedNeed need = new ShoppingInputParser.InterpretedNeed("Tofu", null, new BigDecimal("2"), null, null, null, null);
        long categoryId = jdbc.sql("SELECT id FROM category WHERE household_id = 1 ORDER BY display_order LIMIT 1").query(Long.class).single();

        ShoppingNeedOutcome result = createCustomProduct.execute(list.id(), need, categoryId);

        assertThat(result).isInstanceOf(ShoppingNeedOutcome.Confirmed.class);
        assertThat(jdbc.sql("SELECT COUNT(*) FROM catalog_product WHERE household_id = 1 AND normalized_name = 'tofu' AND origin = 'CUSTOM'").query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void givenUnsupportedInput_whenExecutingAddNeedUseCase_thenNotificationExplainsReformulation() {
        ShoppingList list = createList.execute("Groceries").list();

        ShoppingNeedOutcome result = addNeed.execute(list.id(), "melk 2 3l");

        assertThat(result).isInstanceOfSatisfying(ShoppingNeedOutcome.Rejected.class, rejected ->
                assertThat(rejected.notification().issues()).extracting("code", "message")
                        .containsExactly(tuple("shopping-need.input.unsupported", "Use one quantity, for example: melk 2x1l.")));
    }
}
