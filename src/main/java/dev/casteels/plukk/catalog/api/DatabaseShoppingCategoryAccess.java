package dev.casteels.plukk.catalog.api;

import java.util.Collection;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Database implementation of {@link ShoppingCategoryAccess}.
 *
 * <p>Provides access to seeded starter categories from the catalog schema.
 * Categories are fixed and belong to the deployment's household.
 */
@Component
@Profile("!e2e")
final class DatabaseShoppingCategoryAccess implements ShoppingCategoryAccess {

    private final JdbcClient jdbcClient;

    DatabaseShoppingCategoryAccess(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Collection<ShoppingCategory> allCategories() {
        // Assuming single household deployment (household_id = 1)
        return jdbcClient.sql("""
                        SELECT id, stable_key, display_name, display_order
                        FROM category
                        WHERE household_id = 1
                        ORDER BY display_order ASC
                        """)
                .query((resultSet, rowNumber) -> new ShoppingCategory(
                        resultSet.getLong("id"),
                        resultSet.getString("stable_key"),
                        resultSet.getString("display_name"),
                        resultSet.getInt("display_order")))
                .list();
    }

    @Override
    public Optional<ShoppingCategory> byKey(String key) {
        return jdbcClient.sql("""
                        SELECT id, stable_key, display_name, display_order
                        FROM category
                        WHERE household_id = 1 AND stable_key = :key
                        """)
                .param("key", key)
                .query((resultSet, rowNumber) -> new ShoppingCategory(
                        resultSet.getLong("id"),
                        resultSet.getString("stable_key"),
                        resultSet.getString("display_name"),
                        resultSet.getInt("display_order")))
                .optional();
    }
}
