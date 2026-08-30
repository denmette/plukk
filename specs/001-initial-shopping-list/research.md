# Research: Initial Shopping List

## Spring Modulith Boundaries

**Decision**: Use business modules with named API packages and architecture tests.

**Rationale**: Spring Modulith verification rejects module cycles and access to another module's
internals, enforcing the modular-monolith rule.

**Alternatives considered**: Technical-layer packages encourage broad coupling. Microservices
violate the single-container deployment constraint.

**Reference**: [Spring Modulith verification](https://docs.spring.io/spring-modulith/reference/verification.html)

## Mobile PWA and Degraded Connectivity

**Decision**: Use Vaadin PWA support for the manifest, icons, and service worker, with a custom
static offline page and explicit in-app disconnected state. Cache static resources only; do not
queue or replay mutations.

**Rationale**: Vaadin supplies the PWA resources and supports a separate offline page. This
respects the full-Java UI policy and the specified boundary of useful reads without offline writes.

**Alternatives considered**: Offline client-side application views and write synchronization add
out-of-scope client application logic and conflict handling. A SPA is prohibited.

**Reference**: [Vaadin PWA configuration](https://vaadin.com/docs/latest/flow/configuration/pwa)

## Authentication and Household Authorization

**Decision**: Configure Plukk as an OpenID Connect client of an Authentik OAuth2 provider. Map a
successful identity to a Household User and enforce membership in application behavior and views.

**Rationale**: This avoids local passwords while retaining product-specific household roles.

**Alternatives considered**: Local credentials and proxy-only authentication violate the
constitution. Identity-provider groups alone cannot represent local household membership needs.

**Reference**: [Authentik OAuth2 provider](https://docs.goauthentik.io/add-secure-apps/providers/oauth2/)

## Concurrent Shared-List Changes

**Decision**: Persist changes atomically and publish confirmed state. The latest confirmed change
wins when concurrent changes target the same shopping item.

**Rationale**: The clarified rule is predictable and low-friction for a household list.

**Alternatives considered**: Field merging and conflict dialogs add disproportionate complexity.

## Free-Text Shopping Input

**Decision**: Implement parsing as shopping-module behavior for product or variant text plus an
optional quantity, unit, multiplier, package size, or package descriptor. Accept only one
sufficiently confident interpretation; otherwise return reformulation feedback and create no item.

**Rationale**: This supports the specified examples without a broad natural-language system or
silent guessing.

**Alternatives considered**: Separate quantity forms violate the single-short-interaction goal.

## Test Strategy

**Decision**: Use domain/application tests for parser and list rules, PostgreSQL Testcontainers for
persistence and security boundaries, Spring Modulith verification for module rules, and Playwright
Java for mobile browser journeys against the running application and PostgreSQL.

**Rationale**: Every test validates behavior or an explicit architectural constraint. Playwright
supports mobile-device emulation and isolated browser contexts, matching mobile and multi-user use.

**Alternatives considered**: H2, mock-heavy testing, and coverage targets are prohibited.

**References**: [Playwright Java testing](https://playwright.dev/java/docs/writing-tests),
[Playwright mobile emulation](https://playwright.dev/java/docs/codegen)

## Background Jobs

**Decision**: Do not add a background-job dependency in this release. A future durable delayed,
retried, or recurring-work requirement uses JobRunr with PostgreSQL and an ADR.

**Rationale**: The current scope has no background-work need. JobRunr can reuse PostgreSQL without
additional infrastructure when one arises.

**References**: [JobRunr PostgreSQL storage](https://www.jobrunr.io/en/documentation/storage/postgres/),
[JobRunr Spring Boot starter](https://www.jobrunr.io/en/documentation/configuration/spring/)
