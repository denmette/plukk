package dev.casteels.plukk.shopping.list;

import java.time.Instant;

public record ShoppingList(long id, long householdId, String name, Instant createdAt, Instant updatedAt) {

    public ShoppingList {
        name = normalizedName(name);
    }

    public static String normalizedName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A shopping list name is required.");
        }
        return name.trim();
    }
}
