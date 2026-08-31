package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.identity.HouseholdMemberAccess;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
final class ShoppingNeedMembership {
    private final HouseholdMemberAccess members;

    ShoppingNeedMembership(HouseholdMemberAccess members) {
        this.members = members;
    }

    long currentHouseholdId() {
        return members.currentMember().orElseThrow(() -> new AccessDeniedException("Active membership required.")).householdId();
    }
}
