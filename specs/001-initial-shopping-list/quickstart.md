# Quickstart: Initial Shopping List Validation

## Prerequisites

- Java 25 LTS and Maven or Maven Wrapper.
- Docker or compatible container runtime for PostgreSQL and Testcontainers.
- Authentik OIDC provider configured with local application client details.
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
3. Verify unauthenticated requests are redirected to the Authentik sign-in route.
4. Verify only active `MEMBER` rows in `household_member` resolve through the identity-module API.

## Validate Collaboration and Connectivity

1. Verify the shell reports connected or disconnected state without presenting queued mutations as
   saved.
2. Verify `offline.html` is shown when the browser cannot load the application shell.

## Run Automated Verification

```bash
./mvnw test
./mvnw verify
```

Expected outcomes: PostgreSQL Testcontainers migration, membership, and unauthenticated-access
tests; Spring Modulith verification; and a successful Compose-backed application startup.
