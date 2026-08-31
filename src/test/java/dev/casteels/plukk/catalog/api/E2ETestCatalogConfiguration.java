package dev.casteels.plukk.catalog.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * E2E test implementations for catalog APIs with hardcoded test data.
 *
 * <p>Provides starter categories and basic test products without requiring external
 * database access during Playwright and Testcontainers test runs.
 */
@Configuration
@Profile("e2e")
public class E2ETestCatalogConfiguration {

    private static final List<ShoppingCategoryAccess.ShoppingCategory> TEST_CATEGORIES =
            List.of(
                    new ShoppingCategoryAccess.ShoppingCategory(1L, "produce", "Produce", 10),
                    new ShoppingCategoryAccess.ShoppingCategory(2L, "dairy", "Dairy", 20),
                    new ShoppingCategoryAccess.ShoppingCategory(3L, "meat", "Meat", 30),
                    new ShoppingCategoryAccess.ShoppingCategory(4L, "bakery", "Bakery", 40),
                    new ShoppingCategoryAccess.ShoppingCategory(5L, "drinks", "Drinks", 50),
                    new ShoppingCategoryAccess.ShoppingCategory(6L, "pantry", "Pantry", 60));

    private static final List<CatalogProductAccess.CatalogProduct> TEST_STARTER_PRODUCTS =
            List.of(
                    new CatalogProductAccess.CatalogProduct(1L, "Kip", 3L,
                            CatalogProductAccess.Origin.STARTER),
                    new CatalogProductAccess.CatalogProduct(2L, "Melk", 2L,
                            CatalogProductAccess.Origin.STARTER),
                    new CatalogProductAccess.CatalogProduct(3L, "Appels", 1L,
                            CatalogProductAccess.Origin.STARTER),
                    new CatalogProductAccess.CatalogProduct(4L, "Water", 5L,
                            CatalogProductAccess.Origin.STARTER),
                    new CatalogProductAccess.CatalogProduct(5L, "Cola", 5L,
                            CatalogProductAccess.Origin.STARTER),
                    new CatalogProductAccess.CatalogProduct(6L, "Kaas", 6L,
                            CatalogProductAccess.Origin.STARTER));

    @Bean
    ShoppingCategoryAccess e2eTestShoppingCategoryAccess() {
        return new ShoppingCategoryAccess() {
            @Override
            public Collection<ShoppingCategoryAccess.ShoppingCategory> allCategories() {
                return TEST_CATEGORIES;
            }

            @Override
            public Optional<ShoppingCategoryAccess.ShoppingCategory> byKey(String key) {
                return TEST_CATEGORIES.stream().filter(c -> c.key().equals(key)).findFirst();
            }
        };
    }

    @Bean
    CatalogProductAccess e2eTestCatalogProductAccess() {
        return new CatalogProductAccess() {
            @Override
            public Collection<CatalogProductAccess.CatalogProduct> searchStarter(String searchName) {
                String normalized = searchName.trim().toLowerCase();
                return TEST_STARTER_PRODUCTS.stream()
                        .filter(p -> p.name().toLowerCase().startsWith(normalized))
                        .toList();
            }

            @Override
            public CatalogProductAccess.CatalogProduct findOrCreateCustomProduct(Long householdId,
                                                                                   String productName,
                                                                                   Long categoryId) {
                // E2E tests don't persist, just return a synthetic product
                return new CatalogProductAccess.CatalogProduct(
                        System.nanoTime(), // Unique synthetic ID
                        productName,
                        categoryId,
                        CatalogProductAccess.Origin.CUSTOM);
            }
        };
    }
}
