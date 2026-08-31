package dev.casteels.plukk.identity.api;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * Spring Security implementation of {@link AuthenticatedSubject}.
 *
 * <p>Extracts the OIDC subject claim (sub) from the current Spring Security authentication,
 * which in production is provided by an external OIDC provider (e.g., Authentik).
 */
@Component
@Profile("!e2e")
final class DatabaseAuthenticatedSubject implements AuthenticatedSubject {

    @Override
    public Optional<String> currentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return Optional.of(extractSubject(authentication));
    }

    private String extractSubject(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return principal instanceof OidcUser oidcUser
                ? oidcUser.getSubject()
                : authentication.getName();
    }
}
