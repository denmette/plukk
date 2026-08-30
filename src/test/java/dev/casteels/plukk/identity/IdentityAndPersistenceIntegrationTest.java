package dev.casteels.plukk.identity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.casteels.plukk.PlukkApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
class IdentityAndPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private HouseholdMemberAccess householdMemberAccess;

    @Autowired
    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @AfterEach
    void givenSecurityContext_whenTestCompletes_thenClearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenFreshPostgreSqlDatabase_whenFlywayRuns_thenSchemaAndStarterCatalogExist() {
        Integer householdCount = jdbcClient.sql("SELECT COUNT(*) FROM household")
                .query(Integer.class)
                .single();
        Integer categoryCount = jdbcClient.sql("SELECT COUNT(*) FROM category")
                .query(Integer.class)
                .single();
        Integer starterProductCount = jdbcClient.sql("SELECT COUNT(*) FROM catalog_product WHERE origin = 'STARTER'")
                .query(Integer.class)
                .single();

        assertThat(householdCount).isEqualTo(1);
        assertThat(categoryCount).isEqualTo(6);
        assertThat(starterProductCount).isGreaterThanOrEqualTo(6);
    }

    @Test
    void givenAuthenticatedActiveMember_whenCurrentMemberIsResolved_thenDatabaseMembershipIsReturned() {
        jdbcClient.sql("""
                        INSERT INTO household_member (household_id, external_subject, display_name, role)
                        VALUES (1, 'member-subject', 'Ada Member', 'MEMBER')
                        """)
                .update();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("member-subject", "not-used", "ROLE_USER"));

        assertThat(householdMemberAccess.currentMember())
                .hasValueSatisfying(member -> {
                    assertThat(member.householdId()).isEqualTo(1L);
                    assertThat(member.subject()).isEqualTo("member-subject");
                    assertThat(member.displayName()).isEqualTo("Ada Member");
                });
    }

    @Test
    void givenUnauthenticatedRequest_whenProtectedRouteIsRequested_thenSignInIsRequired() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/"))
                .GET()
                .build();
        HttpResponse<Void> response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build()
                .send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode()).isBetween(300, 399);
    }
}
