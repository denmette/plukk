package dev.casteels.plukk.shopping.list;

import dev.casteels.plukk.shared.notification.Notification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OpenShoppingListUseCase {

    private final ShoppingListMembership membership;
    private final ShoppingListRepository repository;

    OpenShoppingListUseCase(ShoppingListMembership membership, ShoppingListRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Result execute(long listId) {
        return repository.findById(membership.currentHouseholdId(), listId)
                .map(list -> new Result(list, Notification.success()))
                .orElseGet(() -> new Result(null, Notification.issue("shopping-list.not-found", "Shopping list not found.")));
    }

    public record Result(ShoppingList list, Notification notification) {
    }
}
