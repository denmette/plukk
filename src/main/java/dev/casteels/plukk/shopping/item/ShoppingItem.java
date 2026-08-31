package dev.casteels.plukk.shopping.item;

import java.math.BigDecimal;

public record ShoppingItem(
        long id,
        long shoppingListId,
        long catalogProductId,
        String variant,
        BigDecimal quantity,
        String unit,
        BigDecimal packageSize,
        String packageUnit,
        String packageDescriptor,
        State state) {

    public enum State { ACTIVE, PURCHASED }
}
