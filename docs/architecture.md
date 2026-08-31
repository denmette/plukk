# Architecture

Plukk is a self-hosted, single-household Spring Boot and Vaadin Flow modular monolith with
PostgreSQL. The governing requirements are in the
[constitution](../.specify/memory/constitution.md); these guides explain their implementation.

Persistent business state is held in PostgreSQL, not the application container filesystem or
lifecycle. Vaadin Flow and Spring Security may use the runtime and HTTP-session state they require.

## Architecture Guides

- [Module boundaries](architecture/module-boundaries.md): Spring Modulith modules, exposed APIs,
  dependency declarations, and module tests.
- [Application layer](architecture/application-layer.md): Use Cases, Notifications, and the UI
  interaction boundary.
- [Testing](architecture/testing.md): behavioral, module, persistence, and end-to-end testing.
- [Persistence](architecture/persistence.md): PostgreSQL, Flyway, and module-owned data.

## Evolving Module Map

The current business-capability map is a candidate architecture, not an immutable package plan:
`identity`, `household`, `catalog`, shopping capabilities such as `shopping.list`,
`shopping.input`, `shopping.item`, and `shopping.history`, `collaboration`, and `preferences`.
Split, merge, or rename modules only when independent business evolution and an explicit public API
justify the boundary.

## Runtime Topology

```mermaid
flowchart LR
    Phone[Mobile browser or PWA] --> App[Plukk application container]
    Authentik[Authentik OIDC provider] --> App
    App --> Postgres[(PostgreSQL)]
```

The runtime remains intentionally small: one application container plus PostgreSQL. Authentik is
an external identity provider rather than application infrastructure.
