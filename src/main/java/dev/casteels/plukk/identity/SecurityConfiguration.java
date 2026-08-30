package dev.casteels.plukk.identity;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import dev.casteels.plukk.shared.ui.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("!e2e")
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));
        http.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true));
        http.logout(logout -> logout.logoutSuccessUrl("/login"));
        return http.build();
    }

}
