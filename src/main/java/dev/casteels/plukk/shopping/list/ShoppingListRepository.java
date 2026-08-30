package dev.casteels.plukk.shopping.list;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class ShoppingListRepository {

    private final JdbcClient jdbcClient;

    ShoppingListRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    ShoppingList create(long householdId, String name) {
        return jdbcClient.sql("""
                        INSERT INTO shopping_list (household_id, name)
                        VALUES (:householdId, :name)
                        RETURNING id, household_id, name, created_at, updated_at
                        """)
                .param("householdId", householdId)
                .param("name", name)
                .query(this::map)
                .single();
    }

    List<ShoppingList> findAll(long householdId) {
        return jdbcClient.sql("""
                        SELECT id, household_id, name, created_at, updated_at
                        FROM shopping_list WHERE household_id = :householdId ORDER BY created_at, id
                        """)
                .param("householdId", householdId)
                .query(this::map)
                .list();
    }

    Optional<ShoppingList> findById(long householdId, long listId) {
        return jdbcClient.sql("""
                        SELECT id, household_id, name, created_at, updated_at
                        FROM shopping_list WHERE household_id = :householdId AND id = :listId
                        """)
                .param("householdId", householdId)
                .param("listId", listId)
                .query(this::map)
                .optional();
    }

    Optional<ShoppingList> rename(long householdId, long listId, String name) {
        return jdbcClient.sql("""
                        UPDATE shopping_list SET name = :name, updated_at = CURRENT_TIMESTAMP
                        WHERE household_id = :householdId AND id = :listId
                        RETURNING id, household_id, name, created_at, updated_at
                        """)
                .param("householdId", householdId)
                .param("listId", listId)
                .param("name", name)
                .query(this::map)
                .optional();
    }

    boolean delete(long householdId, long listId) {
        return jdbcClient.sql("DELETE FROM shopping_list WHERE household_id = :householdId AND id = :listId")
                .param("householdId", householdId)
                .param("listId", listId)
                .update() == 1;
    }

    private ShoppingList map(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new ShoppingList(
                resultSet.getLong("id"), resultSet.getLong("household_id"), resultSet.getString("name"),
                timestamp(resultSet.getTimestamp("created_at")), timestamp(resultSet.getTimestamp("updated_at")));
    }

    private Instant timestamp(Timestamp timestamp) {
        return timestamp.toInstant();
    }
}
