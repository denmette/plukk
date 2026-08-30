package dev.casteels.plukk.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.identity.HouseholdMemberAccess;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        classes = {PlukkApplication.class, ShoppingListManagementE2ETest.E2eConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers
class ShoppingListManagementE2ETest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private JdbcClient jdbcClient;
    @LocalServerPort private int port;

    private Playwright playwright;
    private Browser browser;
    private Page page;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @BeforeEach
    void givenMobileMember_whenListJourneyStarts_thenOpenMobileBrowser() {
        jdbcClient.sql("DELETE FROM shopping_list").update();
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setExecutablePath(Path.of("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")));
        page = browser.newPage(new Browser.NewPageOptions().setViewportSize(390, 844));
    }

    @AfterEach
    void givenMobileBrowser_whenListJourneyCompletes_thenCloseBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void givenMobileMember_whenManagingLists_thenCreateRenameOpenAndDeleteLists() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/lists")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        page.navigate("http://localhost:" + port + "/lists");

        createList("Groceries");
        createList("Pharmacy");
        var renamedName = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Rename Groceries"));
        page.waitForResponse(
                uidlResponse -> uidlResponse.url().contains("v-r=uidl")
                        && uidlResponse.request().postData().contains("Weekly groceries"),
                () -> renamedName.fill("Weekly groceries"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Rename Groceries")).click();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Weekly groceries").setExact(true)).click();
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Weekly groceries")).waitFor();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("All lists")).click();
        page.waitForResponse(
                uidlResponse -> uidlResponse.url().contains("v-r=uidl")
                        && !uidlResponse.text().contains("Pharmacy"),
                () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete Pharmacy")).click());

        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Weekly groceries").setExact(true)).count())
                .isEqualTo(1);
        assertThat(page.getByText("Pharmacy", new Page.GetByTextOptions().setExact(true)).count()).isZero();
        assertThat(page.evaluate("window.innerWidth")).isEqualTo(390);
    }

    private void createList(String name) {
        page.waitForResponse(
                uidlResponse -> uidlResponse.url().contains("v-r=uidl") && uidlResponse.text().contains(name),
                () -> {
                    page.getByLabel("New list").fill(name);
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create list")).click();
                });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class E2eConfiguration {

        @Bean
        SecurityFilterChain e2eSecurityFilterChain(HttpSecurity http) throws Exception {
            http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                    .enableNavigationAccessControl(false)
                    .enableAuthorizedRequestsConfiguration(false));
            http.addFilterBefore((request, response, chain) -> {
                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                        "e2e-member", "not-used", AuthorityUtils.createAuthorityList("ROLE_USER")));
                try {
                    chain.doFilter(request, response);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }, AnonymousAuthenticationFilter.class);
            http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return http.build();
        }

        @Bean
        @Primary
        HouseholdMemberAccess e2eHouseholdMemberAccess() {
            return () -> Optional.of(new HouseholdMemberAccess.ActiveHouseholdMember(1L, 1L, "e2e-member", "E2E Member"));
        }
    }
}
