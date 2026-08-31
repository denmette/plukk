package dev.casteels.plukk.shopping.item;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.history.ShoppingHistoryRepository;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;

/**
 * Marks an active shopping item purchased while keeping it visible on the list.
 */
@Component
public class PurchaseShoppingItemUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final ShoppingItemRepository repository;
    private final ShoppingHistoryRepository historyRepository;

    PurchaseShoppingItemUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList, ShoppingItemRepository repository,
            ShoppingHistoryRepository historyRepository) {
        this.authUser = authUser;
        this.openList = openList;
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public Result execute(long listId, long itemId) {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new Result(null, Notification.issue("identity.unauthorized", "Active membership required."));
        }
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new Result(null, listNotification);

        return repository.markPurchased(listId, itemId)
                .map(item -> {
                    historyRepository.recordPurchase(user.get().householdId(), item);
                    return new Result(item, Notification.success());
                })
                .orElseGet(() -> new Result(null, Notification.issue("shopping-item.not-found", "Shopping item not found.")));
    }

    public record Result(ShoppingItem item, Notification notification) {}
}
