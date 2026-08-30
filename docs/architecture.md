# Architecture

Plukk is a modular monolith built with Spring Boot, Vaadin Flow, PostgreSQL, and Spring Modulith.
One deployment serves one household.

## Module Boundaries

```mermaid
flowchart LR
    Identity --> Shared
    Household --> Identity
    Household --> Shared
    Catalog --> Household
    Catalog --> Identity
    Catalog --> Shared
    Shopping --> Catalog
    Shopping --> Household
    Shopping --> Identity
    Shopping --> Shared
    Collaboration --> Shopping
    Collaboration --> Identity
    Collaboration --> Shared
```

- `identity`: authentication integration and active-member resolution.
- `household`: household-scoped ownership concepts.
- `catalog`: fixed categories and reusable products.
- `shopping`: lists, items, parsing, and shopping workflows.
- `collaboration`: confirmed shared-list event publication and connectivity feedback.
- `shared`: concrete cross-cutting UI and infrastructure utilities only.

## Authentication Boundary

```mermaid
flowchart TD
    Browser --> Vaadin[Vaadin Flow UI]
    Vaadin --> Security[Spring Security + OIDC client]
    Security --> Authentik[Authentik]
    Vaadin --> Shopping[Shopping application services]
    Shopping --> Identity[Household member access API]
    Shopping --> Postgres[(PostgreSQL)]
```

Authenticated requests enter through Spring Security and Vaadin Flow. Application behavior uses the
identity-module API to resolve the current active household member before reading or mutating
household data.

## Runtime Topology

```mermaid
flowchart LR
    UserPhone[Mobile browser or PWA] --> App[Plukk application container]
    Authentik[Authentik OIDC provider] --> App
    App --> Postgres[(PostgreSQL)]
```

The runtime topology stays intentionally small: one application container plus PostgreSQL.
