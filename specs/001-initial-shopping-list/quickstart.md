# Quickstart: Initial Shopping List Validation

## Prerequisites

- Java 25 LTS and Maven or Maven Wrapper.
- Docker or compatible container runtime for PostgreSQL and Testcontainers.
- Authentik OIDC provider configured with local application client details for production-like runs,
  or the isolated development/test identity profile for local and automated validation.
- Playwright browser binaries installed for the test environment.

## Start the Supporting Services

1. Ensure Docker is running; `./mvnw spring-boot:run` automatically starts `compose.yaml` and waits
   for PostgreSQL to become healthy.
2. Configure Authentik OIDC settings through environment variables or mounted secrets.
3. Start the application with `./mvnw spring-boot:run`.
4. Open the local application on desktop and mobile-sized browser viewports.

## Validate Core Behavior

1. Verify the application starts on port 8080 with the Compose-managed PostgreSQL instance.
2. Verify Flyway applies the household, catalog, shopping, and history baseline schema.
3. Verify unauthenticated production-profile requests are redirected to the Authentik sign-in route.
4. Verify active `OWNER` and `MEMBER` rows can access household shopping data, while `GUEST` and
   inactive rows cannot. Repeat this authorization check with the isolated development/test identity
   profile; it must not be selectable in production.

## Validate Collaboration and Connectivity

1. Verify the shell reports connected or disconnected state without presenting queued mutations as
   saved.
2. Verify `offline.html` is shown when the browser cannot load the application shell.
3. Verify one authorized household user can purchase a need and another can re-add it from the
   household-wide recent-needs list with its concrete details preserved.

## Run Automated Verification

```bash
./mvnw test
./mvnw verify
```

Expected outcomes: PostgreSQL Testcontainers migration, membership, and unauthenticated-access
tests; Spring Modulith verification; and a successful Compose-backed application startup.
