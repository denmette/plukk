package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.identity.HouseholdMemberAccess;
import dev.casteels.plukk.shopping.item.ShoppingItem;
import dev.casteels.plukk.shopping.list.ShoppingListApplicationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddShoppingNeedApplicationService {

    private final HouseholdMemberAccess members;
    private final ShoppingListApplicationService lists;
    private final ShoppingInputParser parser;
    private final JdbcClient jdbc;

    AddShoppingNeedApplicationService(HouseholdMemberAccess members, ShoppingListApplicationService lists, ShoppingInputParser parser, JdbcClient jdbc) {
        this.members = members;
        this.lists = lists;
        this.parser = parser;
        this.jdbc = jdbc;
    }

    @Transactional
    public AddResult add(long listId, String input) {
        long householdId = householdId();
        lists.open(listId);
        ShoppingInputParser.ParseResult parsed = parser.parse(input);
        if (parsed instanceof ShoppingInputParser.ReformulationRequired feedback) {
            return new Reformulation(feedback.message());
        }
        ShoppingInputParser.InterpretedNeed need = (ShoppingInputParser.InterpretedNeed) parsed;
        return findProduct(householdId, need.product())
                .<AddResult>map(productId -> addResolved(listId, productId, need))
                .orElseGet(() -> new CustomProductRequired(need));
    }

    @Transactional
    public AddResult addCustomProduct(long listId, ShoppingInputParser.InterpretedNeed need, long categoryId) {
        long householdId = householdId();
        lists.open(listId);
        boolean categoryExists = jdbc.sql("SELECT EXISTS (SELECT 1 FROM category WHERE id = :categoryId AND household_id = :householdId)")
                .param("categoryId", categoryId).param("householdId", householdId).query(Boolean.class).single();
        if (!categoryExists) {
            throw new IllegalArgumentException("Choose a category from this household.");
        }
        Long productId = jdbc.sql("""
                        INSERT INTO catalog_product (household_id, category_id, name, normalized_name, origin)
                        VALUES (:householdId, :categoryId, :name, :normalizedName, 'CUSTOM') RETURNING id
                        """)
                .param("householdId", householdId).param("categoryId", categoryId)
                .param("name", need.product()).param("normalizedName", normalized(need.product()))
                .query(Long.class).single();
        return addResolved(listId, productId, need);
    }

    @Transactional(readOnly = true)
    public List<Category> availableCategories() {
        long householdId = householdId();
        return jdbc.sql("SELECT id, display_name FROM category WHERE household_id = :householdId ORDER BY display_order")
                .param("householdId", householdId)
                .query((rs, row) -> new Category(rs.getLong("id"), rs.getString("display_name"))).list();
    }

    @Transactional(readOnly = true)
    public String productName(long productId) {
        long householdId = householdId();
        return jdbc.sql("SELECT name FROM catalog_product WHERE id = :productId AND household_id = :householdId")
                .param("productId", productId).param("householdId", householdId).query(String.class).optional()
                .orElseThrow(() -> new AccessDeniedException("Product is not available to this household."));
    }

    private AddResult addResolved(long listId, long productId, ShoppingInputParser.InterpretedNeed need) {
        Optional<Long> existing = jdbc.sql("""
                        SELECT id FROM shopping_item WHERE shopping_list_id = :listId AND catalog_product_id = :productId
                        AND normalized_variant = :variant AND COALESCE(quantity, -1) = COALESCE(:quantity, -1)
                        AND COALESCE(unit, '') = COALESCE(:unit, '') AND COALESCE(package_size, -1) = COALESCE(:packageSize, -1)
                        AND COALESCE(package_unit, '') = COALESCE(:packageUnit, '')
                        AND COALESCE(package_descriptor, '') = COALESCE(:packageDescriptor, '') AND state = 'ACTIVE'
                        """).param("listId", listId).param("productId", productId).param("variant", normalized(need.variant()))
                .param("quantity", need.quantity()).param("unit", need.unit()).param("packageSize", need.packageSize())
                .param("packageUnit", need.packageUnit()).param("packageDescriptor", need.packageDescriptor()).query(Long.class).optional();
        if (existing.isPresent()) return new Duplicate(existing.get());
        ShoppingItem item = jdbc.sql("""
                        INSERT INTO shopping_item (shopping_list_id, catalog_product_id, variant, normalized_variant, quantity, unit, package_size, package_unit, package_descriptor, state)
                        VALUES (:listId, :productId, :variant, :normalizedVariant, :quantity, :unit, :packageSize, :packageUnit, :packageDescriptor, 'ACTIVE')
                        RETURNING id, shopping_list_id, catalog_product_id, variant, quantity, unit, package_size, package_unit, package_descriptor, state
                        """).param("listId", listId).param("productId", productId).param("variant", need.variant())
                .param("normalizedVariant", normalized(need.variant())).param("quantity", need.quantity()).param("unit", need.unit())
                .param("packageSize", need.packageSize()).param("packageUnit", need.packageUnit()).param("packageDescriptor", need.packageDescriptor())
                .query((rs, row) -> new ShoppingItem(rs.getLong("id"), rs.getLong("shopping_list_id"), rs.getLong("catalog_product_id"), rs.getString("variant"), rs.getBigDecimal("quantity"), rs.getString("unit"), rs.getBigDecimal("package_size"), rs.getString("package_unit"), rs.getString("package_descriptor"), ShoppingItem.State.valueOf(rs.getString("state")))).single();
        return new Confirmed(item);
    }

    private Optional<Long> findProduct(long householdId, String name) {
        return jdbc.sql("SELECT id FROM catalog_product WHERE household_id = :householdId AND normalized_name = :name AND active = TRUE")
                .param("householdId", householdId).param("name", normalized(name)).query(Long.class).optional();
    }
    private long householdId() { return members.currentMember().orElseThrow(() -> new AccessDeniedException("Active membership required.")).householdId(); }
    private String normalized(String value) { return value == null ? "" : value.toLowerCase(java.util.Locale.ROOT).trim(); }

    public sealed interface AddResult permits Confirmed, Duplicate, CustomProductRequired, Reformulation {}
    public record Confirmed(ShoppingItem item) implements AddResult {}
    public record Duplicate(long itemId) implements AddResult {}
    public record CustomProductRequired(ShoppingInputParser.InterpretedNeed need) implements AddResult {}
    public record Reformulation(String message) implements AddResult {}
    public record Category(long id, String name) {}
}
