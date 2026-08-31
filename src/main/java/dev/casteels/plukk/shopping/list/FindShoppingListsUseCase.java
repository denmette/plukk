package dev.casteels.plukk.shopping.list;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FindShoppingListsUseCase {

    private final ShoppingListMembership membership;
    private final ShoppingListRepository repository;

    FindShoppingListsUseCase(ShoppingListMembership membership, ShoppingListRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ShoppingList> execute() {
        return repository.findAll(membership.currentHouseholdId());
    }
}
