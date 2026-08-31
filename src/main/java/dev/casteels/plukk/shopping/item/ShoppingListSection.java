package dev.casteels.plukk.shopping.item;

import java.util.List;

/**
 * A category grouping of shopping items for grouped list display.
 */
public record ShoppingListSection(long categoryId, String categoryName, List<Entry> items) {

    public ShoppingListSection {
        items = List.copyOf(items);
    }

    /**
     * A shopping item paired with its catalog product name for display.
     */
    public record Entry(ShoppingItem item, String productName) {}
}
