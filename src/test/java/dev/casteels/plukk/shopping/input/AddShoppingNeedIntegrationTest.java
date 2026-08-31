package dev.casteels.plukk.shopping.input;

import static org.assertj.core.api.Assertions.assertThat;

import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.shopping.list.ShoppingList;
import dev.casteels.plukk.shopping.list.ShoppingListApplicationService;
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
    @Autowired private ShoppingListApplicationService lists;
    @Autowired private AddShoppingNeedApplicationService needs;

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
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void givenSeededCatalogProduct_whenAddingNeed_thenConfirmedItemIsPersisted() {
        ShoppingList list = lists.create("Groceries");

        AddShoppingNeedApplicationService.AddResult result = needs.add(list.id(), "kipfilet 400g");

        assertThat(result).isInstanceOfSatisfying(AddShoppingNeedApplicationService.Confirmed.class, confirmed -> {
            assertThat(confirmed.item().variant()).isEqualTo("Kipfilet");
            assertThat(confirmed.item().quantity()).isEqualByComparingTo(new BigDecimal("400"));
            assertThat(confirmed.item().unit()).isEqualTo("gram");
        });
    }

    @Test
    void givenExactActiveNeed_whenAddingItAgain_thenExistingItemIsReturned() {
        ShoppingList list = lists.create("Groceries");
        AddShoppingNeedApplicationService.Confirmed first = (AddShoppingNeedApplicationService.Confirmed) needs.add(list.id(), "melk 2x1l");

        AddShoppingNeedApplicationService.AddResult result = needs.add(list.id(), "melk 2x1l");

        assertThat(result).isEqualTo(new AddShoppingNeedApplicationService.Duplicate(first.item().id()));
        assertThat(jdbc.sql("SELECT COUNT(*) FROM shopping_item WHERE shopping_list_id = :listId").param("listId", list.id()).query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void givenMissingCatalogProduct_whenAddingCustomProduct_thenHouseholdProductAndItemArePersisted() {
        ShoppingList list = lists.create("Groceries");
        ShoppingInputParser.InterpretedNeed need = new ShoppingInputParser.InterpretedNeed("Tofu", null, new BigDecimal("2"), null, null, null, null);
        Long categoryId = jdbc.sql("SELECT id FROM category WHERE household_id = 1 ORDER BY display_order LIMIT 1").query(Long.class).single();

        AddShoppingNeedApplicationService.AddResult result = needs.addCustomProduct(list.id(), need, categoryId);

        assertThat(result).isInstanceOf(AddShoppingNeedApplicationService.Confirmed.class);
        assertThat(jdbc.sql("SELECT COUNT(*) FROM catalog_product WHERE household_id = 1 AND normalized_name = 'tofu' AND origin = 'CUSTOM'").query(Integer.class).single()).isEqualTo(1);
    }
}
