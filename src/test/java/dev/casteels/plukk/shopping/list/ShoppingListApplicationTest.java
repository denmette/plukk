package dev.casteels.plukk.shopping.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.casteels.plukk.PlukkApplication;
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
class ShoppingListApplicationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private JdbcClient jdbcClient;
    @Autowired private ShoppingListApplicationService lists;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @BeforeEach
    void givenActiveMember_whenListBehaviorRuns_thenAuthenticateMember() {
        jdbcClient.sql("DELETE FROM shopping_list").update();
        jdbcClient.sql("DELETE FROM household_member WHERE external_subject = 'application-member'").update();
        jdbcClient.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'application-member', 'Application Member', 'MEMBER')").update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("application-member", "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenAuthenticatedMember_whenCreatingRenamingOpeningAndDeletingLists_thenRemainingListIsAvailable() {
        ShoppingList groceries = lists.create("Groceries");
        ShoppingList pharmacy = lists.create("Pharmacy");

        ShoppingList renamed = lists.rename(groceries.id(), "Weekly groceries");
        ShoppingList opened = lists.open(renamed.id());
        lists.delete(pharmacy.id());

        assertThat(opened.name()).isEqualTo("Weekly groceries");
        assertThat(lists.lists()).extracting(ShoppingList::name).containsExactly("Weekly groceries");
    }

    @Test
    void givenBlankListName_whenCreatingList_thenInputIsRejected() {
        assertThatThrownBy(() -> lists.create("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A shopping list name is required.");
    }
}
