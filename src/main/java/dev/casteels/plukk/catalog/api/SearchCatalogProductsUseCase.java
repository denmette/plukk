package dev.casteels.plukk.catalog.api;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.casteels.plukk.catalog.product.CatalogProductRepository;
import dev.casteels.plukk.household.api.AuthorizedHouseholdUser;
import dev.casteels.plukk.shared.notification.Notification;

/**
 * Searches the household catalog (starter and custom products) by name.
 */
@Component
public class SearchCatalogProductsUseCase {

    private final AuthorizedHouseholdUser authUser;
    private final CatalogProductRepository repository;

    SearchCatalogProductsUseCase(AuthorizedHouseholdUser authUser, CatalogProductRepository repository) {
        this.authUser = authUser;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Result execute(String query) {
        var user = authUser.currentUser();
        if (user.isEmpty()) {
            return new Result(List.of(), Notification.issue("identity.unauthorized", "Active membership required."));
        }
        if (query == null || query.isBlank()) {
            return new Result(List.of(), Notification.success());
        }
        return new Result(repository.search(user.get().householdId(), query), Notification.success());
    }

    public record Result(List<CatalogProductMatch> matches, Notification notification) {}

    /**
     * A catalog product match for display in search results.
     *
     * @param id the database identifier
     * @param name the product name
     * @param categoryName the display name of the product's category
     * @param visualReference a visual recognition aid (e.g. icon key), may be null
     * @param origin whether this is a STARTER or CUSTOM product
     */
    public record CatalogProductMatch(long id, String name, String categoryName, String visualReference,
            dev.casteels.plukk.catalog.product.CatalogProduct.Origin origin) {}
}
