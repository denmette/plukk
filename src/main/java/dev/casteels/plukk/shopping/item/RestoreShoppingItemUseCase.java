package dev.casteels.plukk.shopping.item;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;

/**
 * Returns a purchased shopping item to the active state.
 */
@Component
public class RestoreShoppingItemUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final ShoppingItemRepository repository;

    RestoreShoppingItemUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList, ShoppingItemRepository repository) {
        this.authUser = authUser;
        this.openList = openList;
        this.repository = repository;
    }

    @Transactional
    public Result execute(long listId, long itemId) {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new Result(null, Notification.issue("identity.unauthorized", "Active membership required."));
        }
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new Result(null, listNotification);

        try {
            return repository.markActive(listId, itemId)
                    .map(item -> new Result(item, Notification.success()))
                    .orElseGet(() -> new Result(null, Notification.issue("shopping-item.not-found", "Shopping item not found.")));
        } catch (DataIntegrityViolationException conflict) {
            // An identical item is already active; restoring this one would duplicate it.
            return new Result(null, Notification.issue("shopping-item.duplicate", "An identical item is already active on this list."));
        }
    }

    public record Result(ShoppingItem item, Notification notification) {}
}
