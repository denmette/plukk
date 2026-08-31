package dev.casteels.plukk.identity.api;

import java.util.Optional;

/**
 * Framework-independent representation of the currently authenticated subject.
 *
 * <p>This interface provides access to the authenticated user's stable external subject
 * identifier without coupling callers to Spring Security, Vaadin, or other authentication
 * frameworks. The external subject is typically an OIDC subject claim (sub) from an identity
 * provider like Authentik.
 */
public interface AuthenticatedSubject {

    /**
     * Returns the stable external subject identifier of the currently authenticated user.
     *
     * @return the authenticated subject's external identifier (e.g., OIDC sub claim), or empty
     *         if not authenticated
     */
    Optional<String> currentSubject();
}
