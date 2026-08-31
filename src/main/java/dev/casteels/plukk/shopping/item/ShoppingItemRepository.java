package dev.casteels.plukk.shopping.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Shopping-owned persistence adapter for grouped reads and reversible item mutations.
 *
 * <p>Joins catalog product and category data for display purposes only; category assignment and
 * product naming remain owned by the catalog module.
 */
@Repository
class ShoppingItemRepository {

    private static final String COLUMNS = "id, shopping_list_id, catalog_product_id, variant, quantity, unit, "
            + "package_size, package_unit, package_descriptor, state";

    private final JdbcClient jdbc;

    ShoppingItemRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Finds all items on the list grouped by category in display order, active items first within
     * each category.
     */
    List<ShoppingListSection> findSections(long householdId, long listId) {
        List<Row> rows = jdbc.sql("""
                        SELECT si.id, si.shopping_list_id, si.catalog_product_id, si.variant, si.quantity, si.unit,
                               si.package_size, si.package_unit, si.package_descriptor, si.state,
                               cp.name AS product_name, c.id AS category_id, c.display_name AS category_name
                        FROM shopping_item si
                        JOIN catalog_product cp ON cp.id = si.catalog_product_id AND cp.household_id = :householdId
                        JOIN category c ON c.id = cp.category_id AND c.household_id = :householdId
                        WHERE si.shopping_list_id = :listId
                        ORDER BY c.display_order, CASE si.state WHEN 'ACTIVE' THEN 0 ELSE 1 END, si.id
                        """)
                .param("householdId", householdId).param("listId", listId)
                .query((rs, row) -> new Row(rs.getLong("category_id"), rs.getString("category_name"), map(rs, row), rs.getString("product_name")))
                .list();

        Map<Long, List<ShoppingListSection.Entry>> entriesByCategory = new LinkedHashMap<>();
        Map<Long, String> categoryNames = new LinkedHashMap<>();
        for (Row row : rows) {
            categoryNames.putIfAbsent(row.categoryId(), row.categoryName());
            entriesByCategory.computeIfAbsent(row.categoryId(), id -> new ArrayList<>())
                    .add(new ShoppingListSection.Entry(row.item(), row.productName()));
        }
        return categoryNames.entrySet().stream()
                .map(entry -> new ShoppingListSection(entry.getKey(), entry.getValue(), entriesByCategory.get(entry.getKey())))
                .toList();
    }

    Optional<ShoppingItem> markPurchased(long listId, long itemId) {
        return jdbc.sql("UPDATE shopping_item SET state = 'PURCHASED', updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = :itemId AND shopping_list_id = :listId RETURNING " + COLUMNS)
                .param("itemId", itemId).param("listId", listId).query(this::map).optional();
    }

    Optional<ShoppingItem> markActive(long listId, long itemId) {
        return jdbc.sql("UPDATE shopping_item SET state = 'ACTIVE', updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = :itemId AND shopping_list_id = :listId RETURNING " + COLUMNS)
                .param("itemId", itemId).param("listId", listId).query(this::map).optional();
    }

    boolean delete(long listId, long itemId) {
        return jdbc.sql("DELETE FROM shopping_item WHERE id = :itemId AND shopping_list_id = :listId")
                .param("itemId", itemId).param("listId", listId).update() == 1;
    }

    private ShoppingItem map(ResultSet rs, int row) throws SQLException {
        return new ShoppingItem(rs.getLong("id"), rs.getLong("shopping_list_id"), rs.getLong("catalog_product_id"),
                rs.getString("variant"), rs.getBigDecimal("quantity"), rs.getString("unit"), rs.getBigDecimal("package_size"),
                rs.getString("package_unit"), rs.getString("package_descriptor"), ShoppingItem.State.valueOf(rs.getString("state")));
    }

    private record Row(long categoryId, String categoryName, ShoppingItem item, String productName) {}
}
