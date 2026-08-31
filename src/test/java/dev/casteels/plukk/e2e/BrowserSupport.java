package dev.casteels.plukk.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Launches a headless Chromium browser for Playwright E2E tests.
 *
 * <p>Prefers a system-installed Chrome/Chromium binary when one exists at a known location
 * (covering macOS developer machines and common Linux package-manager installs). Falls back to
 * Playwright's own managed browser otherwise, which requires the environment's browser
 * dependencies to already be installed (see README.md).
 */
final class BrowserSupport {

    private static final List<String> CANDIDATE_EXECUTABLES = List.of(
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            "/usr/bin/google-chrome-stable",
            "/usr/bin/google-chrome",
            "/usr/bin/chromium-browser",
            "/usr/bin/chromium",
            "/snap/bin/chromium");

    private BrowserSupport() {
    }

    static Browser launch(Playwright playwright) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
        CANDIDATE_EXECUTABLES.stream().map(Path::of).filter(Files::exists).findFirst().ifPresent(options::setExecutablePath);
        return playwright.chromium().launch(options);
    }
}
