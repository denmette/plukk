package dev.casteels.plukk.household.api;

import dev.casteels.plukk.identity.api.AuthenticatedSubject;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Database implementation of {@link AuthorizedHouseholdUser}.
 *
 * <p>Looks up the current user's household membership and enforces role-based authorization:
 * only active OWNER and MEMBER users return authorized access. GUEST users and inactive
 * members return empty.
 */
@Component
@Profile("!e2e")
final class DatabaseHouseholdAuthorization implements AuthorizedHouseholdUser {

    private final AuthenticatedSubject authenticatedSubject;
    private final JdbcClient jdbcClient;

    DatabaseHouseholdAuthorization(AuthenticatedSubject authenticatedSubject, JdbcClient jdbcClient) {
        this.authenticatedSubject = authenticatedSubject;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<HouseholdUserAccess> currentUser() {
        return authenticatedSubject
                .currentSubject()
                .flatMap(this::findAuthorizedMember);
    }

    private Optional<HouseholdUserAccess> findAuthorizedMember(String subject) {
        return jdbcClient
                .sql("""
                        SELECT id, household_id, external_subject, display_name, role
                        FROM household_member
                        WHERE external_subject = :subject AND active = TRUE
                        """)
                .param("subject", subject)
                .query((resultSet, rowNumber) -> new MemberRecord(
                        resultSet.getLong("id"),
                        resultSet.getLong("household_id"),
                        resultSet.getString("external_subject"),
                        resultSet.getString("display_name"),
                        resultSet.getString("role")))
                .list()
                .stream()
                .filter(member -> !member.role().equals("GUEST"))
                .map(member -> new HouseholdUserAccess(
                        member.householdId(),
                        member.id(),
                        member.subject(),
                        member.displayName(),
                        Role.valueOf(member.role())))
                .findFirst();
    }

    private record MemberRecord(Long id, Long householdId, String subject, String displayName, String role) {}
}
