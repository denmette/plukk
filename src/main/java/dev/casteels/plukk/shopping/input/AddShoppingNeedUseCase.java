package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.list.OpenShoppingListUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AddShoppingNeedUseCase {

    private final ShoppingNeedMembership membership;
    private final OpenShoppingListUseCase openList;
    private final ShoppingInputParser parser;
    private final ShoppingNeedRepository repository;

    AddShoppingNeedUseCase(ShoppingNeedMembership membership, OpenShoppingListUseCase openList,
            ShoppingInputParser parser, ShoppingNeedRepository repository) {
        this.membership = membership;
        this.openList = openList;
        this.parser = parser;
        this.repository = repository;
    }

    @Transactional
    public ShoppingNeedOutcome execute(long listId, String input) {
        Notification listNotification = openList.execute(listId).notification();
        if (!listNotification.isSuccess()) return new ShoppingNeedOutcome.Rejected(listNotification);
        ShoppingInputParser.ParseResult parsed = parser.parse(input);
        if (parsed instanceof ShoppingInputParser.ReformulationRequired feedback) {
            return new ShoppingNeedOutcome.Rejected(Notification.issue("shopping-need.input.unsupported", feedback.message()));
        }
        ShoppingInputParser.InterpretedNeed need = (ShoppingInputParser.InterpretedNeed) parsed;
        return repository.findProductId(membership.currentHouseholdId(), need.product())
                .<ShoppingNeedOutcome>map(productId -> persist(listId, productId, need))
                .orElseGet(() -> new ShoppingNeedOutcome.CustomProductRequired(need,
                        Notification.issue("shopping-need.product.missing", "Create a household product to add this need.")));
    }

    private ShoppingNeedOutcome persist(long listId, long productId, ShoppingInputParser.InterpretedNeed need) {
        return repository.findExactActiveItem(listId, productId, need)
                .<ShoppingNeedOutcome>map(itemId -> new ShoppingNeedOutcome.Duplicate(itemId,
                        Notification.issue("shopping-need.duplicate", "This item is already on the list.")))
                .orElseGet(() -> new ShoppingNeedOutcome.Confirmed(repository.createItem(listId, productId, need), Notification.success()));
    }
}
