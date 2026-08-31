package dev.casteels.plukk.shopping.item;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;

/**
 * Reads a shopping list's items grouped by category for display.
 */
@Component
public class GetShoppingListSectionsUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final ShoppingItemRepository repository;

    GetShoppingListSectionsUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList, ShoppingItemRepository repository) {
        this.authUser = authUser;
        this.openList = openList;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Result execute(long listId) {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new Result(List.of(), Notification.issue("identity.unauthorized", "Active membership required."));
        }
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new Result(List.of(), listNotification);

        return new Result(repository.findSections(user.get().householdId(), listId), Notification.success());
    }

    public record Result(List<ShoppingListSection> sections, Notification notification) {}
}
