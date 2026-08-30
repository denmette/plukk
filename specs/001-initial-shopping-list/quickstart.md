# Quickstart: Initial Shopping List Validation

## Prerequisites

- Java 25 LTS and Maven or Maven Wrapper.
- Docker or compatible container runtime for PostgreSQL and Testcontainers.
- Authentik OIDC provider configured with local application client details.
- Playwright browser binaries installed for the test environment.

## Start the Supporting Services

1. Start PostgreSQL with the repository's Docker Compose configuration once added.
2. Configure PostgreSQL and Authentik OIDC settings through environment variables or mounted secrets.
3. Start the application with `./mvnw spring-boot:run`.
4. Open the local application on desktop and mobile-sized browser viewports.

## Validate Core Behavior

1. Sign in as a member and create two lists.
2. Add `kipfilet 400g`; verify `Kip`, `Kipfilet`, `400`, and `gram` appear in an active item.
3. Add `melk 2x1l`, then re-enter it; verify the existing item is focused and unchanged.
4. Enter ambiguous input; verify no item is created and reformulation guidance appears.
5. Create a custom product, select a starter category, and verify another member can find it.
6. Purchase an item, verify it remains visible, then restore it.
7. Re-add a purchased concrete item from recent needs and verify its details are preserved.

## Validate Collaboration and Connectivity

1. Sign in as two members in separate sessions and open the same list.
2. Add, purchase, restore, remove, and change an item; verify the other member sees each confirmed
   result within 3 seconds under normal connectivity.
3. Change the same item nearly simultaneously; verify both sessions show the latest confirmed state.
4. Interrupt one mobile-sized session; verify clear disconnected status and no false saved write.
5. Restore connectivity and verify the list refreshes to the latest confirmed state.

## Run Automated Verification

```bash
./mvnw test
./mvnw verify
```

Expected outcomes: domain and application behavior tests; PostgreSQL Testcontainers tests; Spring
Modulith verification; and Playwright mobile-viewport journeys against the running application and
PostgreSQL all pass.
