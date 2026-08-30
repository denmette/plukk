package dev.casteels.plukk.architecture;

import static org.assertj.core.api.Assertions.assertThatCode;

import dev.casteels.plukk.PlukkApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTest {

    @Test
    void givenApplicationModules_whenVerified_thenBoundariesPass() {
        assertThatCode(() -> ApplicationModules.of(PlukkApplication.class).verify())
                .doesNotThrowAnyException();
    }
}
