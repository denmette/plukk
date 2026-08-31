package dev.casteels.plukk.shopping.item;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;

/**
 * Removes a shopping item from its list.
 */
@Component
public class RemoveShoppingItemUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final ShoppingItemRepository repository;

    RemoveShoppingItemUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList, ShoppingItemRepository repository) {
        this.authUser = authUser;
        this.openList = openList;
        this.repository = repository;
    }

    @Transactional
    public Notification execute(long listId, long itemId) {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return Notification.issue("identity.unauthorized", "Active membership required.");
        }
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return listNotification;

        return repository.delete(listId, itemId)
                ? Notification.success()
                : Notification.issue("shopping-item.not-found", "Shopping item not found.");
    }
}
