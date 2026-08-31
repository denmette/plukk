package dev.casteels.plukk.shopping.list;

import dev.casteels.plukk.shared.notification.Notification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateShoppingListUseCase {

    private final ShoppingListMembership membership;
    private final ShoppingListRepository repository;

    CreateShoppingListUseCase(ShoppingListMembership membership, ShoppingListRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional
    public Result execute(String name) {
        if (name == null || name.isBlank()) {
            return new Result(null, Notification.issue("shopping-list.name.required", "A shopping list name is required."));
        }
        ShoppingList list = repository.create(membership.currentHouseholdId(), name.trim());
        return new Result(list, Notification.success());
    }

    public record Result(ShoppingList list, Notification notification) {
    }
}
