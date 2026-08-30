package dev.casteels.plukk.shopping.list;

import dev.casteels.plukk.identity.HouseholdMemberAccess;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShoppingListApplicationService {

    private final HouseholdMemberAccess householdMemberAccess;
    private final ShoppingListRepository repository;

    ShoppingListApplicationService(HouseholdMemberAccess householdMemberAccess, ShoppingListRepository repository) {
        this.householdMemberAccess = householdMemberAccess;
        this.repository = repository;
    }

    @Transactional
    public ShoppingList create(String name) {
        return repository.create(currentHouseholdId(), ShoppingList.normalizedName(name));
    }

    public List<ShoppingList> lists() {
        return repository.findAll(currentHouseholdId());
    }

    public ShoppingList open(long listId) {
        return repository.findById(currentHouseholdId(), listId).orElseThrow(ShoppingListNotFoundException::new);
    }

    @Transactional
    public ShoppingList rename(long listId, String name) {
        return repository.rename(currentHouseholdId(), listId, ShoppingList.normalizedName(name))
                .orElseThrow(ShoppingListNotFoundException::new);
    }

    @Transactional
    public void delete(long listId) {
        if (!repository.delete(currentHouseholdId(), listId)) {
            throw new ShoppingListNotFoundException();
        }
    }

    private long currentHouseholdId() {
        return householdMemberAccess.currentMember()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Active membership required."))
                .householdId();
    }

    public static class ShoppingListNotFoundException extends RuntimeException {
        ShoppingListNotFoundException() {
            super("Shopping list not found.");
        }
    }
}
