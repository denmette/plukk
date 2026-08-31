package dev.casteels.plukk.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import dev.casteels.plukk.PlukkApplication;
import dev.casteels.plukk.identity.HouseholdMemberAccess;
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
        classes = {PlukkApplication.class, AddShoppingNeedE2ETest.E2eConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers
class AddShoppingNeedE2ETest {

    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private JdbcClient jdbc;
    @LocalServerPort private int port;
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private long listId;

    @DynamicPropertySource
    static void givenPostgreSqlContainer_whenContextStarts_thenConfigureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> false);
    }

    @BeforeEach
    void givenMobileMemberAndEmptyList_whenAddJourneyStarts_thenOpenMobileBrowser() {
        jdbc.sql("DELETE FROM shopping_list").update();
        jdbc.sql("DELETE FROM household_member WHERE external_subject = 'e2e-member'").update();
        jdbc.sql("INSERT INTO household_member (household_id, external_subject, display_name, role) VALUES (1, 'e2e-member', 'E2E Member', 'MEMBER')").update();
        listId = jdbc.sql("INSERT INTO shopping_list (household_id, name) VALUES (1, 'Groceries') RETURNING id").query(Long.class).single();
        playwright = Playwright.create();
        browser = BrowserSupport.launch(playwright);
        page = browser.newPage(new Browser.NewPageOptions().setViewportSize(390, 844));
        page.navigate("http://localhost:" + port + "/lists/" + listId);
    }

    @AfterEach
    void givenMobileBrowser_whenAddJourneyCompletes_thenCloseBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Test
    void givenMobileMember_whenAddingSupportedCustomAndAmbiguousNeeds_thenShowOnlyConfirmedItemsAndFeedback() {
        page.getByLabel("Add a need").fill("kipfilet 400g");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
        page.getByText("Kipfilet - 400 gram", new Page.GetByTextOptions().setExact(true)).waitFor();
        String kipfiletItemId = page.getByText("Kipfilet - 400 gram", new Page.GetByTextOptions().setExact(true))
                .evaluate("element => element.parentElement.id").toString();
        page.getByLabel("Add a need").fill("kipfilet 400g");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
        page.getByText("This item is already on the list.", new Page.GetByTextOptions().setExact(true)).waitFor();
        assertThat(page.evaluate("document.activeElement.id")).isEqualTo(kipfiletItemId);

        page.getByLabel("Add a need").fill("tofu 2");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Create Tofu")).waitFor();
        page.getByLabel("Category").click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Produce").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create product")).click();
        page.getByText("Tofu - 2", new Page.GetByTextOptions().setExact(true)).waitFor();

        int confirmedItems = jdbc.sql("SELECT COUNT(*) FROM shopping_item WHERE shopping_list_id = :listId")
                .param("listId", listId).query(Integer.class).single();
        page.getByLabel("Add a need").fill("melk 2 3l");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
        page.getByText("Use one quantity, for example: melk 2x1l.", new Page.GetByTextOptions().setExact(true)).waitFor();

        assertThat(jdbc.sql("SELECT COUNT(*) FROM shopping_item WHERE shopping_list_id = :listId")
                .param("listId", listId).query(Integer.class).single()).isEqualTo(confirmedItems);
        assertThat(page.evaluate("window.innerWidth")).isEqualTo(390);
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
                try { chain.doFilter(request, response); } finally { SecurityContextHolder.clearContext(); }
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
