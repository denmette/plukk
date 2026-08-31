package dev.casteels.plukk.shopping.input;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.catalog.api.CatalogProductAccess;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;

/**
 * Creates a household-local custom product and immediately adds it as a shopping need item.
 *
 * <p>Orchestrates: list authorization, category validation, custom product creation via
 * catalog API, exact-item duplicate detection, and item persistence. Returns Notification-bearing
 * outcomes for authorization failures, invalid categories, or duplicate items.
 */
@Component
public class CreateCustomProductAndAddShoppingNeedUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final CatalogProductAccess catalog;
    private final ShoppingNeedRepository repository;

    CreateCustomProductAndAddShoppingNeedUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList,
            CatalogProductAccess catalog, ShoppingNeedRepository repository) {
        this.authUser = authUser;
        this.openList = openList;
        this.catalog = catalog;
        this.repository = repository;
    }

    @Transactional
    public ShoppingNeedOutcome execute(long listId, ShoppingInputParser.InterpretedNeed need, long categoryId) {
        // Verify current user is authorized household member
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("identity.unauthorized", "Active membership required."));
        }
        long householdId = user.get().householdId();

        // Verify list belongs to household and is accessible
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new ShoppingNeedOutcome.Rejected(listNotification);

        // Verify category exists in household (catalog API will validate during creation)
        if (!repository.categoryExists(householdId, categoryId)) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("shopping-category.invalid", "Choose a category from this household."));
        }

        // Create household-local custom product via catalog API
        var customProduct = catalog.findOrCreateCustomProduct(householdId, need.product(), categoryId);
        long productId = customProduct.id();

        // Check for exact active duplicate item on this list
        return repository.findExactActiveItem(listId, productId, need)
                .<ShoppingNeedOutcome>map(itemId -> new ShoppingNeedOutcome.Duplicate(itemId,
                        Notification.issue("shopping-need.duplicate", "This item is already on the list.")))
                .orElseGet(() -> new ShoppingNeedOutcome.Confirmed(repository.createItem(listId, productId, need), Notification.success()));
    }
}
