package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.catalog.api.CatalogProductAccess;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds an interpreted shopping need from free-text input to the active shopping list.
 *
 * <p>Orchestrates: list authorization, input parsing, catalog product lookup, exact-item
 * duplicate detection, and item persistence. Returns Notification-bearing outcomes for
 * authorization failures, unsupported input, missing products, or duplicate items.
 */
@Component
public class AddShoppingNeedUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final OpenShoppingListUseCase openList;
    private final ShoppingInputParser parser;
    private final CatalogProductAccess catalog;
    private final ShoppingNeedRepository repository;

    AddShoppingNeedUseCase(AuthorizedHouseholdUser authUser, OpenShoppingListUseCase openList,
            ShoppingInputParser parser, CatalogProductAccess catalog, ShoppingNeedRepository repository) {
        this.authUser = authUser;
        this.openList = openList;
        this.parser = parser;
        this.catalog = catalog;
        this.repository = repository;
    }

    @Transactional
    public ShoppingNeedOutcome execute(long listId, String input) {
        // Verify current user is authorized household member
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("identity.unauthorized", "Active membership required."));
        }
        long householdId = user.get().householdId();

        // Verify list belongs to household and is accessible
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new ShoppingNeedOutcome.Rejected(listNotification);

        // Parse input into structured need or return reformulation feedback
        ShoppingInputParser.ParseResult parsed = parser.parse(input);
        if (parsed instanceof ShoppingInputParser.ReformulationRequired feedback) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("shopping-need.input.unsupported", feedback.message()));
        }
        ShoppingInputParser.InterpretedNeed need = (ShoppingInputParser.InterpretedNeed) parsed;

        // Search catalog for starter product matching the interpreted product name
        var starterProducts = catalog.searchStarter(need.product());
        if (starterProducts.isEmpty()) {
            return new ShoppingNeedOutcome.CustomProductRequired(need,
                    Notification.issue("shopping-need.product.missing", "Create a household product to add this need."));
        }

        // Use first starter product match
        long productId = starterProducts.iterator().next().id();

        // Check for exact active duplicate item on this list
        return repository.findExactActiveItem(listId, productId, need)
                .<ShoppingNeedOutcome>map(itemId -> new ShoppingNeedOutcome.Duplicate(itemId,
                        Notification.issue("shopping-need.duplicate", "This item is already on the list.")))
                .orElseGet(() -> new ShoppingNeedOutcome.Confirmed(repository.createItem(listId, productId, need), Notification.success()));
    }
}
