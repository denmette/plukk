package dev.casteels.plukk.identity;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
final class DatabaseHouseholdMemberAccess implements HouseholdMemberAccess {

    private final JdbcClient jdbcClient;

    DatabaseHouseholdMemberAccess(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<ActiveHouseholdMember> currentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String subject = subjectOf(authentication);
        return jdbcClient.sql("""
                        SELECT id, household_id, external_subject, display_name
                        FROM household_member
                        WHERE external_subject = :subject AND active = TRUE AND role = 'MEMBER'
                        """)
                .param("subject", subject)
                .query((resultSet, rowNumber) -> new ActiveHouseholdMember(
                        resultSet.getLong("household_id"),
                        resultSet.getLong("id"),
                        resultSet.getString("external_subject"),
                        resultSet.getString("display_name")))
                .list()
                .stream()
                .findFirst();
    }

    private String subjectOf(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return principal instanceof OidcUser oidcUser ? oidcUser.getSubject() : authentication.getName();
    }
}
