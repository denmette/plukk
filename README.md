# Plukk

Plukk is a self-hosted shopping-list application for one household per deployment. The product is
planned as a Spring Boot and Vaadin Flow modular monolith with PostgreSQL persistence.

## Verified Build Baseline

As of 2026-08-30, the repository is bootstrapped with these verified stable build-tool versions:

- Java 25 LTS as the target JDK baseline from the active specification
- Apache Maven 3.9.16 via Maven Wrapper
- Apache Maven Wrapper 3.3.4 using the `only-script` distribution type
- Spring Boot 4.1.1
- Vaadin 25.2.6
- Spring Modulith 2.1.1
- Testcontainers 2.0.5
- Playwright for Java 1.60.0
- PostgreSQL JDBC 42.7.13

Use `./mvnw -v` to confirm the local wrapper installation and `./mvnw <goal>` for future project
builds once a JDK is installed locally.

## Entry Point

The application entry point is `dev.casteels.plukk.PlukkApplication` in
`src/main/java/dev/casteels/plukk/PlukkApplication.java`. It boots the Spring Boot application and
declares the initial Vaadin PWA metadata for the household shopping experience.

## Developer Prerequisites

- Java 25
- Docker for PostgreSQL and Testcontainers-backed integration tests
- Access to an Authentik OIDC provider for authenticated flows
- Playwright browser binaries for end-to-end coverage

## Local Run

Docker Compose support is intentional. Starting the application from an IDE or with
`./mvnw spring-boot:run` starts the PostgreSQL 17 service in `compose.yaml`, waits for its health
check, and keeps it running after the application stops. Set the `PLUKK_DB_*` and `PLUKK_OIDC_*`
environment variables when using an external PostgreSQL instance or Authentik provider.

Run `./mvnw verify` with Docker running to execute the PostgreSQL Testcontainers integration suite
and Spring Modulith architecture verification.

## Running E2E Tests on Linux

Playwright E2E tests prefer a system-installed Chrome/Chromium binary (see
`src/test/java/dev/casteels/plukk/e2e/BrowserSupport.java`) and fall back to Playwright's own
managed browser otherwise. On a bare Linux host or container, install one of:

- A system browser via the package manager, e.g. `apt-get install -y chromium` or
  `google-chrome-stable`, or
- Playwright's managed browser and its native dependencies: `npx playwright install --with-deps chromium`

Without either, `./mvnw verify` fails with a Playwright "Host system is missing dependencies to run
browsers" error while trying to launch its bundled, dependency-less browser download.

## Production Identity Configuration

Plukk authenticates users through an external Authentik OIDC provider. The following environment
variables configure the connection:

### Required Authentik Environment Variables

```
PLUKK_OIDC_CLIENT_ID              # Client ID registered in Authentik for this deployment
PLUKK_OIDC_CLIENT_SECRET          # Client secret registered in Authentik (keep secure)
PLUKK_OIDC_AUTHORIZATION_URI      # Authentik authorization endpoint (e.g., https://auth.example.com/application/o/authorize/)
PLUKK_OIDC_TOKEN_URI              # Authentik token endpoint (e.g., https://auth.example.com/application/o/token/)
PLUKK_OIDC_USER_INFO_URI          # Authentik userinfo endpoint (e.g., https://auth.example.com/application/o/userinfo/)
PLUKK_OIDC_JWK_SET_URI            # Authentik JWKS endpoint (e.g., https://auth.example.com/application/o/jwks/)
PLUKK_SECURITY_ALLOWED_CLOCK_SKEW # Maximum clock skew for token validation (default: PT30S)
```

### Authentik Setup

1. In Authentik, create an OAuth2/OIDC provider and application
2. Configure the redirect URI as `https://your-domain.com/login/oauth2/code/authentik`
3. Request scopes: `openid`, `profile`, `email`
4. Record the client ID and secret
5. Set the six OIDC endpoint URIs from your Authentik instance

### Development and Test Identity

Development and test environments use isolated identity profiles that cannot activate in
production. These profiles:

- Provide built-in test users without requiring external Authentik
- Allow local/CI testing without Authentik credentials
- Are explicitly disabled in production deployments
- Preserve household authorization rules during testing

**Development Profile** (`-Dspring.profiles.active=dev`):
- Provides local test identity with hardcoded user subject and household ID
- Useful for rapid iteration during feature development
- Requires no Authentik access

**Test Profile** (`application-e2e.yml`):
- Isolated identity configuration for Testcontainers and Playwright tests
- Provides repeatable test users with predictable household access
- Cannot be activated in production by configuration guard
