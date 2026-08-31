package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.shopping.item.ShoppingItem;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class ShoppingNeedRepository {

    private final JdbcClient jdbc;

    ShoppingNeedRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    Optional<Long> findProductId(long householdId, String name) {
        return jdbc.sql("SELECT id FROM catalog_product WHERE household_id = :householdId AND normalized_name = :name AND active = TRUE")
                .param("householdId", householdId).param("name", normalized(name)).query(Long.class).optional();
    }

    boolean categoryExists(long householdId, long categoryId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM category WHERE id = :categoryId AND household_id = :householdId)")
                .param("categoryId", categoryId).param("householdId", householdId).query(Boolean.class).single();
    }

    long createCustomProduct(long householdId, long categoryId, String name) {
        return jdbc.sql("""
                        INSERT INTO catalog_product (household_id, category_id, name, normalized_name, origin)
                        VALUES (:householdId, :categoryId, :name, :normalizedName, 'CUSTOM') RETURNING id
                        """)
                .param("householdId", householdId).param("categoryId", categoryId)
                .param("name", name).param("normalizedName", normalized(name)).query(Long.class).single();
    }

    Optional<Long> findExactActiveItem(long listId, long productId, ShoppingInputParser.InterpretedNeed need) {
        return jdbc.sql("""
                        SELECT id FROM shopping_item WHERE shopping_list_id = :listId AND catalog_product_id = :productId
                        AND normalized_variant = :variant AND COALESCE(quantity, -1) = COALESCE(:quantity, -1)
                        AND COALESCE(unit, '') = COALESCE(:unit, '') AND COALESCE(package_size, -1) = COALESCE(:packageSize, -1)
                        AND COALESCE(package_unit, '') = COALESCE(:packageUnit, '')
                        AND COALESCE(package_descriptor, '') = COALESCE(:packageDescriptor, '') AND state = 'ACTIVE'
                        """).param("listId", listId).param("productId", productId).param("variant", normalized(need.variant()))
                .param("quantity", need.quantity()).param("unit", need.unit()).param("packageSize", need.packageSize())
                .param("packageUnit", need.packageUnit()).param("packageDescriptor", need.packageDescriptor()).query(Long.class).optional();
    }

    ShoppingItem createItem(long listId, long productId, ShoppingInputParser.InterpretedNeed need) {
        return jdbc.sql("""
                        INSERT INTO shopping_item (shopping_list_id, catalog_product_id, variant, normalized_variant, quantity, unit, package_size, package_unit, package_descriptor, state)
                        VALUES (:listId, :productId, :variant, :normalizedVariant, :quantity, :unit, :packageSize, :packageUnit, :packageDescriptor, 'ACTIVE')
                        RETURNING id, shopping_list_id, catalog_product_id, variant, quantity, unit, package_size, package_unit, package_descriptor, state
                        """).param("listId", listId).param("productId", productId).param("variant", need.variant())
                .param("normalizedVariant", normalized(need.variant())).param("quantity", need.quantity()).param("unit", need.unit())
                .param("packageSize", need.packageSize()).param("packageUnit", need.packageUnit()).param("packageDescriptor", need.packageDescriptor())
                .query((rs, row) -> new ShoppingItem(rs.getLong("id"), rs.getLong("shopping_list_id"), rs.getLong("catalog_product_id"), rs.getString("variant"), rs.getBigDecimal("quantity"), rs.getString("unit"), rs.getBigDecimal("package_size"), rs.getString("package_unit"), rs.getString("package_descriptor"), ShoppingItem.State.valueOf(rs.getString("state")))).single();
    }

    List<ShoppingCategory> findCategories(long householdId) {
        return jdbc.sql("SELECT id, display_name FROM category WHERE household_id = :householdId ORDER BY display_order")
                .param("householdId", householdId).query((rs, row) -> new ShoppingCategory(rs.getLong("id"), rs.getString("display_name"))).list();
    }

    Optional<String> findProductName(long householdId, long productId) {
        return jdbc.sql("SELECT name FROM catalog_product WHERE id = :productId AND household_id = :householdId")
                .param("productId", productId).param("householdId", householdId).query(String.class).optional();
    }

    private String normalized(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
