package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateCustomProductAndAddShoppingNeedUseCase {

    private final ShoppingNeedMembership membership;
    private final OpenShoppingListUseCase openList;
    private final ShoppingNeedRepository repository;

    CreateCustomProductAndAddShoppingNeedUseCase(ShoppingNeedMembership membership, OpenShoppingListUseCase openList,
            ShoppingNeedRepository repository) {
        this.membership = membership;
        this.openList = openList;
        this.repository = repository;
    }

    @Transactional
    public ShoppingNeedOutcome execute(long listId, ShoppingInputParser.InterpretedNeed need, long categoryId) {
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new ShoppingNeedOutcome.Rejected(listNotification);
        long householdId = membership.currentHouseholdId();
        if (!repository.categoryExists(householdId, categoryId)) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("shopping-category.invalid", "Choose a category from this household."));
        }
        long productId = repository.createCustomProduct(householdId, categoryId, need.product());
        return repository.findExactActiveItem(listId, productId, need)
                .<ShoppingNeedOutcome>map(itemId -> new ShoppingNeedOutcome.Duplicate(itemId,
                        Notification.issue("shopping-need.duplicate", "This item is already on the list.")))
                .orElseGet(() -> new ShoppingNeedOutcome.Confirmed(repository.createItem(listId, productId, need), Notification.success()));
    }
}
