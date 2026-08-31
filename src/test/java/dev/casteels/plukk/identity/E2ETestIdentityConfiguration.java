package dev.casteels.plukk.identity;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser.HouseholdUserAccess;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser.Role;
import dev.casteels.plukk.identity.api.AuthenticatedSubject;

/**
 * E2E test identity configuration for Testcontainers and Playwright tests.
 *
 * <p>Provides isolated test identities with repeatable household access and roles.
 * This configuration is explicitly guarded to never activate in production and is only
 * available when the "e2e" Spring profile is active.
 *
 * <p>Activated by: setting Spring profile to "e2e" in tests only
 */
@Configuration
@Profile("e2e")
public class E2ETestIdentityConfiguration {

    // Fixed test identities for repeatable test scenarios
    private static final String E2E_TEST_SUBJECT = "e2e-test-subject";
    private static final Long E2E_HOUSEHOLD_ID = 1L;
    private static final Long E2E_MEMBER_ID = 1L;
    private static final String E2E_DISPLAY_NAME = "E2E Test User";

    /**
     * Provides a hardcoded test subject for e2e tests.
     */
    @Bean
    AuthenticatedSubject e2eTestAuthenticatedSubject() {
        return () -> Optional.of(E2E_TEST_SUBJECT);
    }

    /**
     * Provides a hardcoded authorized household user with OWNER role for e2e tests.
     *
     * <p>This allows e2e tests to access household resources without requiring external
     * Authentik. The role and household are fixed to ensure test predictability.
     */
    @Bean
    AuthorizedHouseholdUser e2eTestAuthorizedHouseholdUser() {
        return () -> Optional.of(new HouseholdUserAccess(
                E2E_HOUSEHOLD_ID,
                E2E_MEMBER_ID,
                E2E_TEST_SUBJECT,
                E2E_DISPLAY_NAME,
                Role.OWNER));
    }
}
