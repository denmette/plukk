package dev.casteels.plukk.shopping.input;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;

@Component
public class FindCatalogProductNameUseCase {
    private final AuthorizedHouseholdUser authUser;
    private final ShoppingNeedRepository repository;

    FindCatalogProductNameUseCase(AuthorizedHouseholdUser authUser, ShoppingNeedRepository repository) {
        this.authUser = authUser;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public String execute(long productId) {
        var user = authUser.currentUser()
                .orElseThrow(() -> new AccessDeniedException("Active membership required."));
        return repository.findProductName(user.householdId(), productId)
                .orElseThrow(() -> new AccessDeniedException("Product is not available to this household."));
    }
}
