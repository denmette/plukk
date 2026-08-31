package dev.casteels.plukk.shopping.history;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import dev.casteels.plukk.shopping.item.ShoppingItem;

/**
 * Shopping-owned persistence adapter for household-wide purchase history and re-addition.
 *
 * <p>Public within the shopping module so {@code PurchaseShoppingItemUseCase} (item sub-package)
 * can record history after a confirmed purchase; not exposed outside the shopping module.
 */
@Repository
public class ShoppingHistoryRepository {

    private final JdbcClient jdbc;

    ShoppingHistoryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Creates or refreshes the history entry for this exact concrete need so it sorts as most
     * recent, visible to every authorized household user.
     */
    public void recordPurchase(long householdId, ShoppingItem item) {
        jdbc.sql("""
                        DELETE FROM shopping_history_entry
                        WHERE household_id = :householdId AND catalog_product_id = :productId
                          AND COALESCE(variant, '') = COALESCE(:variant, '') AND COALESCE(quantity, -1) = COALESCE(:quantity, -1)
                          AND COALESCE(unit, '') = COALESCE(:unit, '') AND COALESCE(package_size, -1) = COALESCE(:packageSize, -1)
                          AND COALESCE(package_unit, '') = COALESCE(:packageUnit, '')
                          AND COALESCE(package_descriptor, '') = COALESCE(:packageDescriptor, '')
                        """)
                .param("householdId", householdId).param("productId", item.catalogProductId()).param("variant", item.variant())
                .param("quantity", item.quantity()).param("unit", item.unit()).param("packageSize", item.packageSize())
                .param("packageUnit", item.packageUnit()).param("packageDescriptor", item.packageDescriptor()).update();

        jdbc.sql("""
                        INSERT INTO shopping_history_entry
                            (household_id, catalog_product_id, variant, quantity, unit, package_size, package_unit, package_descriptor)
                        VALUES (:householdId, :productId, :variant, :quantity, :unit, :packageSize, :packageUnit, :packageDescriptor)
                        """)
                .param("householdId", householdId).param("productId", item.catalogProductId()).param("variant", item.variant())
                .param("quantity", item.quantity()).param("unit", item.unit()).param("packageSize", item.packageSize())
                .param("packageUnit", item.packageUnit()).param("packageDescriptor", item.packageDescriptor()).update();
    }

    /**
     * Finds the most recently purchased distinct needs for the household, newest first.
     */
    public List<RecentShoppingNeed> findRecent(long householdId) {
        return jdbc.sql("""
                        SELECT she.id, she.catalog_product_id, cp.name AS product_name, she.variant, she.quantity, she.unit,
                               she.package_size, she.package_unit, she.package_descriptor
                        FROM shopping_history_entry she
                        JOIN catalog_product cp ON cp.id = she.catalog_product_id AND cp.household_id = she.household_id
                        WHERE she.household_id = :householdId
                        ORDER BY she.purchased_at DESC
                        LIMIT 20
                        """)
                .param("householdId", householdId)
                .query(this::mapRecentNeed)
                .list();
    }

    /**
     * Finds an exact active item on the list matching a recent need's copied details.
     */
    public Optional<Long> findExactActiveItem(long listId, RecentShoppingNeed need) {
        return jdbc.sql("""
                        SELECT id FROM shopping_item WHERE shopping_list_id = :listId AND catalog_product_id = :productId
                        AND normalized_variant = :variant AND COALESCE(quantity, -1) = COALESCE(:quantity, -1)
                        AND COALESCE(unit, '') = COALESCE(:unit, '') AND COALESCE(package_size, -1) = COALESCE(:packageSize, -1)
                        AND COALESCE(package_unit, '') = COALESCE(:packageUnit, '')
                        AND COALESCE(package_descriptor, '') = COALESCE(:packageDescriptor, '') AND state = 'ACTIVE'
                        """)
                .param("listId", listId).param("productId", need.catalogProductId()).param("variant", normalized(need.variant()))
                .param("quantity", need.quantity()).param("unit", need.unit()).param("packageSize", need.packageSize())
                .param("packageUnit", need.packageUnit()).param("packageDescriptor", need.packageDescriptor())
                .query(Long.class).optional();
    }

    /**
     * Creates a new active shopping item copying a recent need's details.
     */
    public ShoppingItem createItem(long listId, RecentShoppingNeed need) {
        return jdbc.sql("""
                        INSERT INTO shopping_item (shopping_list_id, catalog_product_id, variant, normalized_variant, quantity, unit, package_size, package_unit, package_descriptor, state)
                        VALUES (:listId, :productId, :variant, :normalizedVariant, :quantity, :unit, :packageSize, :packageUnit, :packageDescriptor, 'ACTIVE')
                        RETURNING id, shopping_list_id, catalog_product_id, variant, quantity, unit, package_size, package_unit, package_descriptor, state
                        """)
                .param("listId", listId).param("productId", need.catalogProductId()).param("variant", need.variant())
                .param("normalizedVariant", normalized(need.variant())).param("quantity", need.quantity()).param("unit", need.unit())
                .param("packageSize", need.packageSize()).param("packageUnit", need.packageUnit()).param("packageDescriptor", need.packageDescriptor())
                .query((resultSet, row) -> new ShoppingItem(resultSet.getLong("id"), resultSet.getLong("shopping_list_id"),
                        resultSet.getLong("catalog_product_id"), resultSet.getString("variant"), resultSet.getBigDecimal("quantity"),
                        resultSet.getString("unit"), resultSet.getBigDecimal("package_size"), resultSet.getString("package_unit"),
                        resultSet.getString("package_descriptor"), ShoppingItem.State.valueOf(resultSet.getString("state"))))
                .single();
    }

    /**
     * Finds a recent need by its history entry identifier, scoped to the household.
     */
    public Optional<RecentShoppingNeed> findRecentById(long householdId, long entryId) {
        return jdbc.sql("""
                        SELECT she.id, she.catalog_product_id, cp.name AS product_name, she.variant, she.quantity, she.unit,
                               she.package_size, she.package_unit, she.package_descriptor
                        FROM shopping_history_entry she
                        JOIN catalog_product cp ON cp.id = she.catalog_product_id AND cp.household_id = she.household_id
                        WHERE she.household_id = :householdId AND she.id = :entryId
                        """)
                .param("householdId", householdId).param("entryId", entryId)
                .query(this::mapRecentNeed)
                .optional();
    }

    private RecentShoppingNeed mapRecentNeed(ResultSet resultSet, int row) throws SQLException {
        return new RecentShoppingNeed(resultSet.getLong("id"), resultSet.getLong("catalog_product_id"), resultSet.getString("product_name"),
                resultSet.getString("variant"), resultSet.getBigDecimal("quantity"), resultSet.getString("unit"),
                resultSet.getBigDecimal("package_size"), resultSet.getString("package_unit"), resultSet.getString("package_descriptor"));
    }

    private String normalized(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * A recently purchased concrete need, with its product name resolved for display.
     */
    public record RecentShoppingNeed(long entryId, long catalogProductId, String productName, String variant, BigDecimal quantity,
            String unit, BigDecimal packageSize, String packageUnit, String packageDescriptor) {}
}
