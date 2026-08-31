package dev.casteels.plukk.catalog.product;

public record CatalogProduct(long id, long householdId, long categoryId, String name, String normalizedName, Origin origin) {

    public CatalogProduct {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A product name is required.");
        }
    }

    public enum Origin { STARTER, CUSTOM }
}
