package dev.casteels.plukk.shopping.history;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.input.ShoppingNeedOutcome;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;

/**
 * Re-adds a household-wide recent need to a list, copying its concrete details.
 *
 * <p>Orchestrates: list authorization, recent-need lookup, exact-item duplicate detection, and
 * item persistence. Returns the same Notification-bearing outcomes as adding a fresh need.
 */
@Component
public class ReAddShoppingNeedUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final ShoppingHistoryRepository repository;

    ReAddShoppingNeedUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList, ShoppingHistoryRepository repository) {
        this.authUser = authUser;
        this.openList = openList;
        this.repository = repository;
    }

    @Transactional
    public ShoppingNeedOutcome execute(long listId, long historyEntryId) {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("identity.unauthorized", "Active membership required."));
        }
        long householdId = user.get().householdId();

        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new ShoppingNeedOutcome.Rejected(listNotification);

        var need = repository.findRecentById(householdId, historyEntryId);
        if (need.isEmpty()) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("shopping-history.not-found", "Recent need not found."));
        }

        return repository.findExactActiveItem(listId, need.get())
                .<ShoppingNeedOutcome>map(itemId -> new ShoppingNeedOutcome.Duplicate(itemId,
                        Notification.issue("shopping-need.duplicate", "This item is already on the list.")))
                .orElseGet(() -> new ShoppingNeedOutcome.Confirmed(repository.createItem(listId, need.get()), Notification.success()));
    }
}
