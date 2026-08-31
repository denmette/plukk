package dev.casteels.plukk.shopping.list;

import dev.casteels.plukk.shared.notification.Notification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteShoppingListUseCase {

    private final ShoppingListMembership membership;
    private final ShoppingListRepository repository;

    DeleteShoppingListUseCase(ShoppingListMembership membership, ShoppingListRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional
    public Result execute(long listId) {
        Notification notification = repository.delete(membership.currentHouseholdId(), listId)
                ? Notification.success()
                : Notification.issue("shopping-list.not-found", "Shopping list not found.");
        return new Result(notification);
    }

    public record Result(Notification notification) {
    }
}
