package dev.casteels.plukk.identity;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import dev.casteels.plukk.identity.api.AuthenticatedSubject;

/**
 * Development-only identity configuration for rapid local iteration.
 *
 * <p>This configuration provides hardcoded test identities without requiring external Authentik.
 * It preserves household authorization rules and cannot be activated in production environments.
 *
 * <p>Activated by: {@code -Dspring.profiles.active=dev}
 * Cannot be used in production because it has no authentication mechanism.
 */
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
@EnableWebSecurity
@EnableMethodSecurity
public class DevelopmentIdentityConfiguration {

    private static final String DEV_SUBJECT = "dev-user";

    /**
     * Provides a hardcoded authenticated subject for development.
     */
    @Bean
    AuthenticatedSubject developmentAuthenticatedSubject() {
        return () -> Optional.of(DEV_SUBJECT);
    }

    /**
     * Minimal security filter chain for development that bypasses OAuth2 login.
     *
     * <p>This allows local testing without Authentik. In production, this configuration
     * is never activated because the "dev" profile is not set.
     */
    @Bean
    SecurityFilterChain developmentSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
}
