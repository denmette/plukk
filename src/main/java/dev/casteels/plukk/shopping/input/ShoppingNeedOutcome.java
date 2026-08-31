package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.shared.notification.Notification;
import dev.casteels.plukk.shopping.item.ShoppingItem;

public sealed interface ShoppingNeedOutcome permits ShoppingNeedOutcome.Confirmed, ShoppingNeedOutcome.Duplicate,
        ShoppingNeedOutcome.CustomProductRequired, ShoppingNeedOutcome.Rejected {

    Notification notification();

    record Confirmed(ShoppingItem item, Notification notification) implements ShoppingNeedOutcome {
    }

    record Duplicate(long itemId, Notification notification) implements ShoppingNeedOutcome {
    }

    record CustomProductRequired(ShoppingInputParser.InterpretedNeed need, Notification notification) implements ShoppingNeedOutcome {
    }

    record Rejected(Notification notification) implements ShoppingNeedOutcome {
    }
}
