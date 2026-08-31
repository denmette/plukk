package dev.casteels.plukk.catalog.api;

import java.util.Collection;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Database implementation of {@link CatalogProductAccess}.
 *
 * <p>Provides starter product search and household-local custom product creation/lookup.
 * Uses case-insensitive normalized name matching and enforces single household per deployment.
 */
@Component
@Profile("!e2e")
final class DatabaseCatalogProductAccess implements CatalogProductAccess {

    private final JdbcClient jdbcClient;

    DatabaseCatalogProductAccess(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Collection<CatalogProduct> searchStarter(String searchName) {
        String normalized = normalize(searchName);
        // Search starter products that start with the normalized prefix
        return jdbcClient.sql("""
                        SELECT id, name, category_id, origin
                        FROM catalog_product
                        WHERE household_id = 1
                          AND active = TRUE
                          AND origin = 'STARTER'
                          AND normalized_name LIKE :prefix
                        ORDER BY normalized_name ASC
                        """)
                .param("prefix", normalized + "%")
                .query((resultSet, rowNumber) -> new CatalogProduct(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getLong("category_id"),
                        CatalogProductAccess.Origin.valueOf(resultSet.getString("origin"))))
                .list();
    }

    @Override
    public CatalogProduct findOrCreateCustomProduct(Long householdId, String productName,
                                                    Long categoryId) {
        String normalized = normalize(productName);

        // Try to find existing custom product
        Optional<CatalogProduct> existing = jdbcClient.sql("""
                        SELECT id, name, category_id, origin
                        FROM catalog_product
                        WHERE household_id = :householdId
                          AND normalized_name = :normalized
                          AND origin = 'CUSTOM'
                        """)
                .param("householdId", householdId)
                .param("normalized", normalized)
                .query((resultSet, rowNumber) -> new CatalogProduct(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getLong("category_id"),
                        CatalogProductAccess.Origin.valueOf(resultSet.getString("origin"))))
                .optional();

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new custom product
        Long newId = jdbcClient.sql("""
                        INSERT INTO catalog_product (household_id, category_id, name, normalized_name, origin)
                        VALUES (:householdId, :categoryId, :name, :normalized, 'CUSTOM')
                        RETURNING id
                        """)
                .param("householdId", householdId)
                .param("categoryId", categoryId)
                .param("name", productName)
                .param("normalized", normalized)
                .query(Long.class)
                .single();

        return new CatalogProduct(newId, productName, categoryId, CatalogProductAccess.Origin.CUSTOM);
    }

    private String normalize(String input) {
        return input.trim().toLowerCase();
    }
}
