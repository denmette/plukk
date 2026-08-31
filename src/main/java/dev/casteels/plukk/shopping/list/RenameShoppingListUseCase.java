package dev.casteels.plukk.shopping.list;

import dev.casteels.plukk.shared.notification.Notification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RenameShoppingListUseCase {

    private final ShoppingListMembership membership;
    private final ShoppingListRepository repository;

    RenameShoppingListUseCase(ShoppingListMembership membership, ShoppingListRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional
    public Result execute(long listId, String name) {
        if (name == null || name.isBlank()) {
            return new Result(null, Notification.issue("shopping-list.name.required", "A shopping list name is required."));
        }
        return repository.rename(membership.currentHouseholdId(), listId, name.trim())
                .map(list -> new Result(list, Notification.success()))
                .orElseGet(() -> new Result(null, Notification.issue("shopping-list.not-found", "Shopping list not found.")));
    }

    public record Result(ShoppingList list, Notification notification) {
    }
}
