package dev.casteels.plukk.shopping.input;

import dev.casteels.plukk.catalog.api.ShoppingCategoryAccess;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FindShoppingCategoriesUseCase {
    private final AuthorizedHouseholdUser authUser;
    private final ShoppingCategoryAccess categories;

    FindShoppingCategoriesUseCase(AuthorizedHouseholdUser authUser, ShoppingCategoryAccess categories) {
        this.authUser = authUser;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<ShoppingCategory> execute() {
        authUser.currentUser()
                .orElseThrow(() -> new AccessDeniedException("Active membership required."));
        return categories.allCategories().stream()
                .map(cat -> new ShoppingCategory(cat.id(), cat.displayName()))
                .toList();
    }
}
