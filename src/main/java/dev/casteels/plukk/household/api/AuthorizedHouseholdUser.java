package dev.casteels.plukk.household.api;

import java.util.Optional;

/**
 * Household-owned API for role-aware authorization.
 *
 * <p>This interface provides access to the current user's household membership and role.
 * It enforces household authorization rules: only active OWNER and MEMBER users can access
 * household resources. GUEST role users are denied access. Authorization failures return empty
 * rather than throwing exceptions, allowing callers to return user-correctable Notifications.
 *
 * <p>This API is household-owned and should be injected by modules that need household
 * authorization decisions.
 */
public interface AuthorizedHouseholdUser {

    /**
     * Returns the current user's authorized household access if authenticated and authorized
     * (active OWNER or MEMBER role).
     *
     * @return the authorized household user with membership and role information, or empty if
     *         not authenticated or not authorized (e.g., GUEST role or inactive)
     */
    Optional<HouseholdUserAccess> currentUser();

    /**
     * Represents an authorized household user with membership and role information.
     *
     * @param householdId the ID of the household this user belongs to
     * @param memberId the ID of the household_member record
     * @param subject the stable external subject identifier (e.g., OIDC sub)
     * @param displayName the user's display name
     * @param role the user's role (OWNER or MEMBER)
     */
    record HouseholdUserAccess(
            Long householdId, Long memberId, String subject, String displayName, Role role) {}

    enum Role {
        OWNER,
        MEMBER,
        GUEST
    }
}
