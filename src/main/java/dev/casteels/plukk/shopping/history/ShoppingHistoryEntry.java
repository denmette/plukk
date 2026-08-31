package dev.casteels.plukk.shopping.history;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A preserved purchased concrete need, retained household-wide for fast re-addition.
 */
public record ShoppingHistoryEntry(
        long id,
        long householdId,
        long catalogProductId,
        String variant,
        BigDecimal quantity,
        String unit,
        BigDecimal packageSize,
        String packageUnit,
        String packageDescriptor,
        Instant purchasedAt) {}
