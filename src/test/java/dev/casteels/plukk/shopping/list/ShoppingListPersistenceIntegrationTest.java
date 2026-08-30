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
class ShoppingListPersistenceIntegrationTest {

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
        jdbcClient.sql("DELETE FROM household_member WHERE external_subject = 'lists-member'").update();
        jdbcClient.sql("DELETE FROM household WHERE id = 2").update();
        jdbcClient.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'lists-member', 'Lists Member', 'MEMBER')").update();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("lists-member", "unused", "ROLE_USER"));
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenAuthenticatedMember_whenManagingLists_thenOnlyOwnedListsChange() {
        ShoppingList groceries = lists.create("Groceries");
        ShoppingList pharmacy = lists.create("Pharmacy");

        ShoppingList renamed = lists.rename(groceries.id(), "Weekly groceries");
        lists.delete(pharmacy.id());

        assertThat(lists.open(groceries.id()).name()).isEqualTo("Weekly groceries");
        assertThat(renamed.name()).isEqualTo("Weekly groceries");
        assertThat(lists.lists()).extracting(ShoppingList::name).containsExactly("Weekly groceries");
    }

    @Test
    void givenListFromAnotherHousehold_whenMemberOpensIt_thenAccessIsRejected() {
        jdbcClient.sql("INSERT INTO household (id, display_name) VALUES (2, 'Other household')").update();
        Long otherListId = jdbcClient.sql("INSERT INTO shopping_list (household_id, name) VALUES (2, 'Private') RETURNING id")
                .query(Long.class).single();

        assertThatThrownBy(() -> lists.open(otherListId))
                .isInstanceOf(ShoppingListApplicationService.ShoppingListNotFoundException.class);
    }
}
