# Implementation Plan: Initial Shopping List

**Branch**: `001-initial-shopping-list` | **Date**: 2026-08-31 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-initial-shopping-list/spec.md`

## Summary

Build Plukk's first useful release as a self-hosted, mobile-first shared shopping-list application
for one household. It authenticates members through an external identity provider, manages lists
and a reusable categorized catalog, parses concise shopping input into concrete items, preserves
recent needs, synchronizes confirmed changes, and clearly communicates degraded connectivity. The
implementation is a Spring Boot and Vaadin Flow modular monolith backed by PostgreSQL.

## Technical Context

**Language/Version**: Java 25 LTS

**Primary Dependencies**: Spring Boot 4.1.1; Vaadin Flow and Spring Modulith current stable
compatible releases; Spring Security OAuth2 client; Spring Data JPA; Flyway; PostgreSQL JDBC

**Storage**: PostgreSQL, with Flyway as the only schema-migration mechanism

**Testing**: JUnit 5, AssertJ, Mockito only for appropriate doubles, Testcontainers PostgreSQL,
Spring Modulith verification, and Playwright Java against the running application and PostgreSQL

**Target Platform**: One Linux application container plus PostgreSQL; persistent business state
does not depend on the application container filesystem or lifecycle; Vaadin Flow and Spring
Security runtime/session state is allowed; modern mobile and desktop browsers; HTTPS in deployed
environments

**Project Type**: Server-rendered web application and installable PWA

**Performance Goals**: 90% of supported shopping needs added and displayed within 15 seconds;
shared-list updates visible within 3 seconds in 95% of normal-connectivity cases

**Constraints**: One household per deployment; approximately 390px mobile-first viewport; no SPA;
no offline writes, mutation queue, or distributed conflict resolution; latest confirmed concurrent
change wins; unconfirmed writes must never appear stored

**Scale/Scope**: One household, active owners and members, multiple lists, starter catalog and
fixed categories, custom products, household-wide recent needs, and no guest invitations or
recurring suggestions

## Constitution Check

| Gate | Status | Plan evidence |
|---|---|---|
| Product identity and household scope | PASS | One deployment owns one household; no tenant or SaaS model. |
| Full-Java Spring Boot and Vaadin Flow architecture | PASS | Server-rendered Vaadin Flow UI; browser code is limited to PWA resources. |
| Modular monolith | PASS | `identity`, `household`, `catalog`, `shopping`, and `collaboration` are capability modules with narrow APIs; `shopping` remains one cohesive capability rather than artificial package modules. |
| Simple deployment | PASS | The runtime comprises one application container and PostgreSQL only. |
| Stable technology policy | PASS | Bootstrap pins current stable compatible releases; previews and snapshots are excluded. |
| Mobile-first and PWA behavior | PASS | E2E flows use a mobile viewport, PWA installability, and explicit disconnected state. |
| Security and persistence | PASS | External OIDC authentication, server-side authorization, PostgreSQL, and Flyway are mandatory. |
| Behavioral testing | PASS | Tests validate domain, integration, architecture, and actual-browser outcomes. |
| Scope discipline | PASS | Offline mutations, guest workflows, recurring suggestions, and unrelated domains remain out of scope. |

**Post-design re-check**: PASS. The data model and contracts give `household`, `catalog`, and
`shopping` distinct persistence ownership, keep `identity` framework-specific and stateless, and
preserve the PostgreSQL-only and offline boundaries. JobRunr is not introduced because this release
has no concrete persistent background-processing requirement.

## Project Structure

### Documentation (this feature)

```text
specs/001-initial-shopping-list/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── shopping-input.md
│   └── ui-behavior.md
└── tasks.md                 # Created later by $speckit-tasks
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/dev/casteels/plukk/
│   │   ├── PlukkApplication.java
│   │   ├── identity/
│   │   ├── household/
│   │   ├── catalog/
│   │   ├── shopping/
│   │   ├── collaboration/
│   │   └── shared/
│   └── resources/
│       ├── db/migration/{household,catalog,shopping}/
│       ├── application.yml
│       └── META-INF/resources/
│           ├── icons/
│           └── offline.html
└── test/java/dev/casteels/plukk/
    ├── architecture/
    ├── identity/
    ├── household/
    ├── catalog/
    ├── shopping/
    ├── collaboration/
    └── e2e/
```

**Structure Decision**: Use one Maven project with capability-driven Spring Modulith modules.
`identity` adapts Spring Security and OIDC authentication to a framework-independent current-subject
API and owns no business tables. `household` owns households, membership, roles, and authorization
decisions; `catalog` owns categories and products; `shopping` owns lists, items, and household-wide
history; `collaboration` has no persistent state and consumes published confirmed changes. Each
state-owning module has its own Flyway location, persistence adapter, PostgreSQL integration test,
and narrow named API. Vaadin views coordinate user interaction only and call public Use Cases or
module APIs. The `shared` package is limited to genuinely cross-cutting types.

## Complexity Tracking

No constitutional violations or exceptions require justification.
