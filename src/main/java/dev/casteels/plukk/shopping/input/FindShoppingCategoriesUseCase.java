package dev.casteels.plukk.shopping.input;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FindShoppingCategoriesUseCase {
    private final ShoppingNeedMembership membership;
    private final ShoppingNeedRepository repository;

    FindShoppingCategoriesUseCase(ShoppingNeedMembership membership, ShoppingNeedRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ShoppingCategory> execute() {
        return repository.findCategories(membership.currentHouseholdId());
    }
}
