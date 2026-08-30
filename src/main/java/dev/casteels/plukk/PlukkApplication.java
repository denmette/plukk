package dev.casteels.plukk;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import dev.casteels.plukk.shared.ui.PlukkAppShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("plukk")
@PWA(
        name = "Plukk",
        shortName = "Plukk",
        description = "Self-hosted household shopping list.",
        offlinePath = "offline.html"
)
public class PlukkApplication implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        PlukkAppShell.configurePage(settings);
    }

    public static void main(String[] args) {
        SpringApplication.run(PlukkApplication.class, args);
    }
}
