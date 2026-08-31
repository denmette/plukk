package dev.casteels.plukk.household;

import static org.assertj.core.api.Assertions.assertThat;

import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser.HouseholdUserAccess;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser.Role;
import dev.casteels.plukk.identity.api.AuthenticatedSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for household-owned role-aware authorization.
 *
 * <p>Verifies that AuthorizedHouseholdUser correctly enforces household authorization:
 * - Active OWNER and MEMBER users are authorized with their respective roles
 * - Inactive users are denied access
 * - GUEST users are denied access
 * - Unauthenticated requests return empty
 */
@SpringBootTest(classes = PlukkApplication.class)
@Testcontainers
class HouseholdAuthorizationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AuthorizedHouseholdUser authorizedHouseholdUser;

    @Autowired
    private AuthenticatedSubject authenticatedSubject;

    @Autowired
    private JdbcClient jdbcClient;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(
            DynamicPropertyRegistry registry) {
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
    void givenActiveMemberUser_whenAuthorizationIsResolved_thenMemberAccessIsReturned() {
        insertHouseholdMember("alice-subject", "Alice", Role.MEMBER, true);
        authenticateAsOidcUser("alice-subject");

        var access = authorizedHouseholdUser.currentUser();

        assertThat(access)
                .hasValueSatisfying(member -> {
                    assertThat(member.householdId()).isEqualTo(1L);
                    assertThat(member.role()).isEqualTo(Role.MEMBER);
                    assertThat(member.displayName()).isEqualTo("Alice");
                    assertThat(member.subject()).isEqualTo("alice-subject");
                });
    }

    @Test
    void givenActiveOwnerUser_whenAuthorizationIsResolved_thenOwnerAccessIsReturned() {
        insertHouseholdMember("owner-subject", "Owner User", Role.OWNER, true);
        authenticateAsOidcUser("owner-subject");

        var access = authorizedHouseholdUser.currentUser();

        assertThat(access)
                .hasValueSatisfying(member -> {
                    assertThat(member.householdId()).isEqualTo(1L);
                    assertThat(member.role()).isEqualTo(Role.OWNER);
                    assertThat(member.displayName()).isEqualTo("Owner User");
                });
    }

    @Test
    void givenInactiveUser_whenAuthorizationIsResolved_thenAccessIsDenied() {
        insertHouseholdMember("inactive-subject", "Inactive User", Role.MEMBER, false);
        authenticateAsOidcUser("inactive-subject");

        var access = authorizedHouseholdUser.currentUser();

        assertThat(access).isEmpty();
    }

    @Test
    void givenGuestUser_whenAuthorizationIsResolved_thenAccessIsDenied() {
        insertHouseholdMember("guest-subject", "Guest User", Role.GUEST, true);
        authenticateAsOidcUser("guest-subject");

        var access = authorizedHouseholdUser.currentUser();

        assertThat(access).isEmpty();
    }

    @Test
    void givenUnauthenticatedRequest_whenAuthorizationIsResolved_thenAccessIsEmpty() {
        var access = authorizedHouseholdUser.currentUser();

        assertThat(access).isEmpty();
    }

    @Test
    void givenNoHouseholdMembership_whenAuthorizationIsResolved_thenAccessIsEmpty() {
        authenticateAsOidcUser("unknown-subject");

        var access = authorizedHouseholdUser.currentUser();

        assertThat(access).isEmpty();
    }

    // Helper methods

    private void insertHouseholdMember(String externalSubject, String displayName, Role role, boolean active) {
        jdbcClient.sql("""
                        INSERT INTO household_member (household_id, external_subject, display_name, role, active)
                        VALUES (1, :subject, :displayName, :role, :active)
                        """)
                .param("subject", externalSubject)
                .param("displayName", displayName)
                .param("role", role.name())
                .param("active", active)
                .update();
    }

    private void authenticateAsOidcUser(String subject) {
        OidcIdToken idToken = OidcIdToken.withTokenValue("test-token")
                .subject(subject)
                .build();
        DefaultOidcUser oidcUser = new DefaultOidcUser(
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                idToken);
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
                        oidcUser,
                        oidcUser.getAuthorities(),
                        "authentik"));
    }
}
