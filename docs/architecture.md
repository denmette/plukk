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
    Vaadin --> Shopping[Shopping Use Cases]
    Shopping --> Identity[Household member access API]
    Shopping --> Postgres[(PostgreSQL)]
```

Authenticated requests enter through Spring Security and Vaadin Flow. Application behavior uses the
identity-module API to resolve the current active household member before reading or mutating
household data.

## Use-Case Application Layer

Application orchestration lives in intent-named `*UseCase` classes. Every Use Case exposes one
public `execute(...)` operation. The existing shopping actions are split by intent, including list
creation, rename, open, deletion, need entry, custom-product creation, and focused read actions.
Repositories, member access, security configuration, and Vaadin components retain their technical
responsibilities and are not Use Cases.

Expected, user-correctable validation and business-rule outcomes are returned as small result
records containing a `Notification`. A notification contains stable issue codes and messages for
the UI to display. Authentication failures, database faults, violated invariants, and other
unexpected technical failures remain exceptions; Vaadin logs them and uses its safe retry feedback
rather than presenting them as successful outcomes.

```mermaid
flowchart LR
    UI[Vaadin UI] --> UC[Intent-named Use Case\nexecute command]
    UC --> DR[Domain and Repository collaborators]
    DR --> UC
    UC --> R[Result plus Notification]
    R --> UI
    UI --> F[Render confirmed state\nor notification feedback]
```

## Runtime Topology

```mermaid
flowchart LR
    UserPhone[Mobile browser or PWA] --> App[Plukk application container]
    Authentik[Authentik OIDC provider] --> App
    App --> Postgres[(PostgreSQL)]
```

The runtime topology stays intentionally small: one application container plus PostgreSQL.
