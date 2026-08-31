package dev.casteels.plukk.shopping.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * E2E test configuration for shopping input module with synthetic product support.
 *
 * <p>Provides mock implementations that handle both seeded catalog products and
 * synthetic test products created during E2E scenarios.
 */
@Configuration
@Profile("e2e")
public class E2ETestShoppingInputConfiguration {

    private static final Map<Long, String> SYNTHETIC_PRODUCTS = new HashMap<>();
    static {
        // Seeded test products
        SYNTHETIC_PRODUCTS.put(1L, "Kip");
        SYNTHETIC_PRODUCTS.put(2L, "Melk");
        SYNTHETIC_PRODUCTS.put(3L, "Appels");
        SYNTHETIC_PRODUCTS.put(4L, "Water");
        SYNTHETIC_PRODUCTS.put(5L, "Cola");
        SYNTHETIC_PRODUCTS.put(6L, "Kaas");
    }

    @Bean
    @Primary
    FindCatalogProductNameUseCase e2eTestFindCatalogProductNameUseCase() {
        return productId -> {
            String name = SYNTHETIC_PRODUCTS.getOrDefault(productId, "Custom Product #" + productId);
            return name;
        };
    }
}
