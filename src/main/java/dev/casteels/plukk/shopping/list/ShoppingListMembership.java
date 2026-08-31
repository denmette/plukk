package dev.casteels.plukk.shopping.list;

import dev.casteels.plukk.identity.HouseholdMemberAccess;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
final class ShoppingListMembership {

    private final HouseholdMemberAccess members;

    ShoppingListMembership(HouseholdMemberAccess members) {
        this.members = members;
    }

    long currentHouseholdId() {
        return members.currentMember()
                .orElseThrow(() -> new AccessDeniedException("Active membership required."))
                .householdId();
    }
}
