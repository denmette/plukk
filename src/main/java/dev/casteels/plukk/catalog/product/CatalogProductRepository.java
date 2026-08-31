package dev.casteels.plukk.catalog.product;

import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import dev.casteels.plukk.catalog.api.SearchCatalogProductsUseCase.CatalogProductMatch;

/**
 * Catalog-owned persistence adapter for product search.
 *
 * <p>Public within the catalog module so {@code SearchCatalogProductsUseCase} (catalog.api
 * package) can use it; not exposed outside the catalog module.
 */
@Repository
public class CatalogProductRepository {

    private final JdbcClient jdbc;

    CatalogProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Searches active starter and custom products by name, joined with category for display.
     *
     * @param householdId the household identifier
     * @param query the search text (matched anywhere in the product name, case-insensitive)
     * @return matching products ordered by name
     */
    public List<CatalogProductMatch> search(long householdId, String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return jdbc.sql("""
                        SELECT cp.id, cp.name, c.display_name AS category_name, cp.visual_reference, cp.origin
                        FROM catalog_product cp
                        JOIN category c ON c.id = cp.category_id AND c.household_id = cp.household_id
                        WHERE cp.household_id = :householdId AND cp.active = TRUE AND cp.normalized_name LIKE :pattern
                        ORDER BY cp.name ASC
                        """)
                .param("householdId", householdId)
                .param("pattern", "%" + normalized + "%")
                .query((resultSet, row) -> new CatalogProductMatch(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("category_name"),
                        resultSet.getString("visual_reference"),
                        CatalogProduct.Origin.valueOf(resultSet.getString("origin"))))
                .list();
    }
}
