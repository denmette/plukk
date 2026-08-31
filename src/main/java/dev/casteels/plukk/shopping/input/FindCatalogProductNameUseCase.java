package dev.casteels.plukk.shopping.input;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FindCatalogProductNameUseCase {
    private final ShoppingNeedMembership membership;
    private final ShoppingNeedRepository repository;

    FindCatalogProductNameUseCase(ShoppingNeedMembership membership, ShoppingNeedRepository repository) {
        this.membership = membership;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public String execute(long productId) {
        return repository.findProductName(membership.currentHouseholdId(), productId)
                .orElseThrow(() -> new AccessDeniedException("Product is not available to this household."));
    }
}
