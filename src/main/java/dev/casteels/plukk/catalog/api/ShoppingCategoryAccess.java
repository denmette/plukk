package dev.casteels.plukk.catalog.api;

import java.util.Collection;
import java.util.Optional;

/**
 * Catalog-owned API for accessing shopping category information.
 *
 * <p>This interface provides access to the fixed starter categories without exposing
 * catalog persistence internals. Categories are seeded at deployment and cannot be
 * created or modified by users.
 */
public interface ShoppingCategoryAccess {

    /**
     * Returns all available shopping categories in display order.
     *
     * @return an immutable collection of all categories
     */
    Collection<ShoppingCategory> allCategories();

    /**
     * Returns a specific category by its stable key.
     *
     * @param key the category key (e.g., "produce", "dairy", "beverages")
     * @return the category, or empty if not found
     */
    Optional<ShoppingCategory> byKey(String key);

    /**
     * A category visible in shopping lists and add interfaces.
     *
     * @param id the database identifier
     * @param key the stable key for this category
     * @param displayName the user-visible name
     * @param displayOrder the sort order
     */
    record ShoppingCategory(Long id, String key, String displayName, Integer displayOrder) {}
}
