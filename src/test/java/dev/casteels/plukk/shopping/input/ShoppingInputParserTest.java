package dev.casteels.plukk.shopping.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ShoppingInputParserTest {

    private final ShoppingInputParser parser = new ShoppingInputParser();

    @Test
    void givenVariantAndGramQuantity_whenParsing_thenReturnsConcreteNeed() {
        assertThat(parser.parse("kipfilet 400g"))
                .isEqualTo(new ShoppingInputParser.InterpretedNeed("Kip", "Kipfilet", new BigDecimal("400"), "gram", null, null, null));
    }

    @Test
    void givenMultiplierAndPackageSize_whenParsing_thenReturnsPackageDetails() {
        assertThat(parser.parse("melk 2x1l"))
                .isEqualTo(new ShoppingInputParser.InterpretedNeed("Melk", null, new BigDecimal("2"), null, new BigDecimal("1"), "liter", null));
    }

    @Test
    void givenCountAndPackageDescriptor_whenParsing_thenReturnsSupportedNeed() {
        assertThat(parser.parse("cola 2 flessen"))
                .isEqualTo(new ShoppingInputParser.InterpretedNeed("Cola", null, new BigDecimal("2"), null, null, null, "flessen"));
    }

    @Test
    void givenCountWithoutUnit_whenParsing_thenRetainsCountWithoutUnit() {
        assertThat(parser.parse("appels 6"))
                .isEqualTo(new ShoppingInputParser.InterpretedNeed("Appels", null, new BigDecimal("6"), null, null, null, null));
    }

    @Test
    void givenDecimalMultiplierAndPackageSize_whenParsing_thenReturnsPackageDetails() {
        assertThat(parser.parse("water 6x1.5l"))
                .isEqualTo(new ShoppingInputParser.InterpretedNeed("Water", null, new BigDecimal("6"), null, new BigDecimal("1.5"), "liter", null));
    }

    @Test
    void givenAmbiguousInput_whenParsing_thenReturnsReformulationFeedback() {
        assertThat(parser.parse("melk 2 3l"))
                .isEqualTo(new ShoppingInputParser.ReformulationRequired("Use one quantity, for example: melk 2x1l."));
    }

    @Test
    void givenUnsupportedInput_whenParsing_thenReturnsReformulationFeedback() {
        assertThat(parser.parse("???"))
                .isEqualTo(new ShoppingInputParser.ReformulationRequired("Use one quantity, for example: melk 2x1l."));
    }
}
