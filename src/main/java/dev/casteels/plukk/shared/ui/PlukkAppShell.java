package dev.casteels.plukk.shared.ui;

import com.vaadin.flow.server.AppShellSettings;

public final class PlukkAppShell {

    private PlukkAppShell() {
    }

    public static void configurePage(AppShellSettings settings) {
        settings.addMetaTag("theme-color", "#7a2e1f");
        settings.addMetaTag("apple-mobile-web-app-capable", "yes");
        settings.addLink("manifest", "manifest.webmanifest");
        settings.addFavIcon("icon", "icons/icon.png", "192x192");
        settings.addLink("apple-touch-icon", "icons/icon.png");
        settings.addMetaTag("apple-mobile-web-app-title", "Plukk");
    }
}
