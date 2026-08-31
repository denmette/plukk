package dev.casteels.plukk.catalog.api;

import java.util.Collection;
import java.util.Optional;

/**
 * Catalog-owned API for accessing products without exposing persistence internals.
 *
 * <p>Provides name-based lookups for starter catalog products and supports household-local
 * custom product creation and lookup. Does not expose catalog repository, entity details,
 * or cross-household visibility.
 */
public interface CatalogProductAccess {

    /**
     * Searches the catalog for starter products matching the given name prefix.
     *
     * <p>Search is case-insensitive. Returns only active, starter-origin products.
     *
     * @param searchName the product name or prefix to search for
     * @return matching products sorted by relevance
     */
    Collection<CatalogProduct> searchStarter(String searchName);

    /**
     * Finds or creates a household-local custom product.
     *
     * <p>If a custom product with this exact name already exists in the household, returns it.
     * Otherwise, creates a new custom product assigned to the given category.
     *
     * @param householdId the household that owns the custom product
     * @param productName the custom product name
     * @param categoryId the category for this product
     * @return the created or found custom product
     */
    CatalogProduct findOrCreateCustomProduct(Long householdId, String productName, Long categoryId);

    /**
     * A product accessible in shopping workflows.
     *
     * @param id the database identifier
     * @param name the product name
     * @param categoryId the category this product belongs to
     * @param origin whether this is a STARTER or CUSTOM product
     */
    record CatalogProduct(Long id, String name, Long categoryId, Origin origin) {}

    enum Origin {
        STARTER,
        CUSTOM
    }
}
