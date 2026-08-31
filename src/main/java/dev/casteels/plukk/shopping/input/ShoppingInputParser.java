package dev.casteels.plukk.shopping.input;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ShoppingInputParser {

    private static final Pattern MULTIPLIER = Pattern.compile("^(.+?)\\s+(\\d+(?:[.,]\\d+)?)x(\\d+(?:[.,]\\d+)?)(g|kg|l|ml)$");
    private static final Pattern QUANTITY_WITH_UNIT = Pattern.compile("^(.+?)\\s+(\\d+(?:[.,]\\d+)?)(g|kg|l|ml)$");
    private static final Pattern QUANTITY_WITH_DESCRIPTOR = Pattern.compile("^(.+?)\\s+(\\d+)\\s+(flessen|pakken)$");
    private static final Pattern COUNT = Pattern.compile("^(.+?)\\s+(\\d+)$");

    public ParseResult parse(String input) {
        if (input == null || input.isBlank()) {
            return reformulation();
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT).replace(',', '.');
        if (!normalized.matches("[a-z0-9.\\s]+")) {
            return reformulation();
        }
        if (normalized.matches(".*\\d+\\s+\\d+.*")) {
            return reformulation();
        }
        Matcher multiplier = MULTIPLIER.matcher(normalized);
        if (multiplier.matches()) {
            return interpreted(multiplier.group(1), decimal(multiplier.group(2)), null, decimal(multiplier.group(3)), unit(multiplier.group(4)));
        }
        Matcher quantityWithUnit = QUANTITY_WITH_UNIT.matcher(normalized);
        if (quantityWithUnit.matches()) {
            return interpreted(quantityWithUnit.group(1), decimal(quantityWithUnit.group(2)), unit(quantityWithUnit.group(3)), null, null);
        }
        Matcher descriptor = QUANTITY_WITH_DESCRIPTOR.matcher(normalized);
        if (descriptor.matches()) {
            return interpreted(descriptor.group(1), decimal(descriptor.group(2)), null, null, null, descriptor.group(3));
        }
        Matcher count = COUNT.matcher(normalized);
        return count.matches()
                ? interpreted(count.group(1), decimal(count.group(2)), null, null, null)
                : interpreted(normalized, null, null, null, null);
    }

    private ParseResult interpreted(String productAndVariant, BigDecimal quantity, String unit, BigDecimal packageSize, String packageUnit) {
        return interpreted(productAndVariant, quantity, unit, packageSize, packageUnit, null);
    }

    private ParseResult interpreted(String productAndVariant, BigDecimal quantity, String unit, BigDecimal packageSize, String packageUnit, String packageDescriptor) {
        if (productAndVariant.startsWith("kip") && !productAndVariant.equals("kip")) {
            return new InterpretedNeed("Kip", title(productAndVariant), quantity, unit, packageSize, packageUnit, packageDescriptor);
        }
        String[] parts = productAndVariant.trim().split("\\s+", 2);
        String product = title(parts[0]);
        String variant = parts.length == 2 ? title(parts[1]) : null;
        return new InterpretedNeed(product, variant, quantity, unit, packageSize, packageUnit, packageDescriptor);
    }

    private String unit(String unit) {
        return switch (unit) {
            case "g" -> "gram";
            case "kg" -> "kilogram";
            case "l" -> "liter";
            case "ml" -> "milliliter";
            default -> unit;
        };
    }

    private BigDecimal decimal(String value) { return new BigDecimal(value); }
    private String title(String value) { return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1); }
    private ReformulationRequired reformulation() { return new ReformulationRequired("Use one quantity, for example: melk 2x1l."); }

    public sealed interface ParseResult permits InterpretedNeed, ReformulationRequired {}
    public record InterpretedNeed(String product, String variant, BigDecimal quantity, String unit, BigDecimal packageSize, String packageUnit, String packageDescriptor) implements ParseResult {}
    public record ReformulationRequired(String message) implements ParseResult {}
}
