<!--
Sync Impact Report
Version change: 1.4.0 -> 1.4.1
Modified principles:
- All principles: concise governing statements; normative intent unchanged
Modified sections:
- Product and Technical Constraints -> Product and Technical Constraints
- Delivery Workflow and Quality Gates -> Delivery Workflow and Quality Gates
Added sections:
- None
Removed sections:
- None
Follow-up TODOs:
- None

No architectural requirements were removed. Duplicate guidance was consolidated, and
implementation-specific guidance moved to supporting documentation. Normative intent is unchanged.
-->
# Plukk Constitution

## Core Principles

### I. Product Identity
Plukk is self-hosted software for one household per deployment. Production Java packages MUST use
`dev.casteels.plukk`; placeholder package names are prohibited. Multi-tenancy and implicit SaaS
evolution are out of scope. This keeps product, ownership, and deployment boundaries clear.

### II. Full-Java Architecture
Java, Spring Boot, and Vaadin Flow MUST provide the application and UI. Separate SPA frontends are
out of scope; minimal browser code is allowed only for necessary PWA or degraded-connectivity
capabilities. This keeps the stack and operational model cohesive.

### III. Modular Monolith
Plukk MUST be a Spring Modulith modular monolith, not microservices. Every independently evolving
vertical slice MUST be a real business-capability module with an explicit public API and declared
allowed dependencies. A module MUST expose only deliberate APIs or domain events; it MUST NOT
access another module's internal domain objects, repositories, adapters, Use Cases, UI, or
persistence implementation. Each module owns its application behavior, persistence changes, and
tests, and MUST be independently testable without unrelated business modules. Automated
architecture tests MUST verify module boundaries. The implementation guide is
[`docs/architecture/module-boundaries.md`](../../docs/architecture/module-boundaries.md).

### IV. Simple Deployment
Production MUST run as one stateless application container and PostgreSQL. Configuration MUST use
environment variables or mounted configuration or secrets. Redis, Kafka, RabbitMQ, Elasticsearch,
Kubernetes, and comparable infrastructure require a concrete documented requirement. This keeps
homelab operation practical.

### V. Modern Stable Technologies
Use stable production releases only. The preferred Java version MUST be the latest stable LTS;
major technology versions MUST be verified when work is performed and recorded in the README.
Pre-release and experimental dependencies require explicit approval. This reduces avoidable
support risk.

### VI. Mobile-First User Experience
Design for approximately a 390px, one-handed smartphone viewport before larger screens. Core
shopping actions MUST minimize effort, use accessible touch targets, remain immediate, and keep
unpurchased and purchased items clearly distinct. Accessibility, keyboard use where relevant, and
reduced-motion preferences are mandatory. This optimizes Plukk for its actual context of use.

### VII. Vaadin Conventions
Prefer standard Vaadin Flow components and focused composed views. Do not create a generic internal
UI framework without a concrete need. Views coordinate presentation only; business rules belong in
the owning domain and application module. This retains separation within the chosen framework.

### VIII. PWA and Degraded Connectivity
Plukk MUST be installable as a PWA and provide useful, clearly communicated degraded behavior
during temporary disconnection. It MUST NOT claim a write succeeded until the server confirms it.
Offline mutation, conflict resolution, synchronization queues, and CRDTs are out of MVP scope
unless separately specified. This prevents misleading or unreliable shopping workflows.

### IX. Household Model
Each installation represents one household. The domain MUST support owner, member, and guest roles,
with server-enforced household permissions; guest access may be limited to selected lists.
Complex invitation workflows are out of MVP scope unless specified. This matches authorization to
the single-household product boundary.

### X. Authentication and Authorization
Authentication MUST use Authentik through OpenID Connect; Plukk MUST NOT manage passwords. Spring
Security owns authentication and server-side authorization. CSRF MUST NOT be disabled for
convenience, and secrets, credentials, tokens, and session identifiers MUST never be logged or
committed. This makes security a product boundary.

### XI. Persistence
PostgreSQL is the authoritative persistent store. Schema changes MUST use Flyway; Hibernate schema
generation is prohibited in production. A module owns its tables, persistence adapter, and Flyway
migrations. Cross-module foreign keys and shared-table ownership need an explicit architecture
exception. One database and Flyway configuration are the default; independent schema or deployment
requires an ADR. The implementation guide is
[`docs/architecture/persistence.md`](../../docs/architecture/persistence.md).

### XII. Behavioral Testing
Tests MUST validate observable behavior, not implementation detail or coverage targets. Java tests
MUST use Given/When/Then and the name
`given<Precondition>_when<Action>_then<ExpectedBehavior>`, with JUnit and AssertJ. Use
Testcontainers for PostgreSQL-dependent integration tests and Playwright against the running
application for applicable critical end-to-end journeys, including representative mobile viewports.
Focused module integration tests MUST prove each slice's exposed behavior, PostgreSQL persistence,
and applicable domain-event interactions; end-to-end tests complement, never replace, module tests.
The implementation guide is [`docs/architecture/testing.md`](../../docs/architecture/testing.md).

### XIII. Vertical Slices
Deliver features as usable vertical slices, not horizontal stages of tables, services, and UI. A
slice that independently evolves MUST own a coherent capability, state, Use Cases, persistence, and
tests as a module. Shopping MUST divide into `shopping.list`, `shopping.input`, `shopping.item`,
and `shopping.history` as their Use Cases evolve independently. This keeps delivery user-visible
and integration risk contained.

### XIV. Domain Simplicity
A catalog entry is reusable; a shopping item is its list placement. A list MUST not accidentally
contain duplicate active entries for the same product. Quantity, optional notes, and reversible
purchases cover MVP variation. Inventory, recipes, meal planning, pricing, integrations, scanning,
and AI are out of scope unless specified. This avoids premature domain expansion.

### XV. Scope Discipline
Implement only the active specification. Do not introduce speculative SaaS, multi-tenant,
microservice, AI, analytics, event-streaming, enterprise, or generalized infrastructure designs.
Extensibility is valid only when it adds no speculative complexity. This protects delivery focus.

### XVI. Code Quality
Organize code by feature and module. Generic base controller, service, repository, Use Case, or
result inheritance frameworks are prohibited without an approved concrete case. Interfaces need a
real boundary, not an implementation counterpart. Favor explicit readable Java, appropriate
records for immutable values, and Spring and Vaadin conventions before customization. This keeps
the codebase legible.

### XVII. Use-Case Application Layer and Notifications
Application orchestration MUST use intent-named `*UseCase` classes, not generic application service
classes. Each Use Case represents one cohesive action and exposes at most one public behavior
operation, normally `execute(...)`; domain behavior and accurately named technical collaborators
remain separate. Use Cases MUST use module-owned domain types and ports, not concrete adapters or
HTTP, Vaadin, JPA, or other framework return types. Expected user-correctable validation and
business-rule failures MUST return a Notification-based outcome; exceptions remain for unexpected
technical, programming, or invariant failures. The implementation guide is
[`docs/architecture/application-layer.md`](../../docs/architecture/application-layer.md).

### XVIII. Documentation Is Part of the Product
Documentation MUST evolve with relevant slices, serve a concrete purpose, and remain current. The
README is the concise entry point; detailed guidance MUST cover applicable setup, architecture,
domain, identity, configuration, persistence, testing, deployment, operations, and PWA behavior.
Use Mermaid source in version-controlled Markdown where a diagram materially improves
understanding. Significant architectural decisions require lightweight ADRs with context, decision,
consequences, and alternatives. This keeps decisions and operation understandable.

### XIX. Background Jobs and Scheduling
When persistent or business-relevant background work is required, JobRunr is preferred and SHOULD
use PostgreSQL persistence where feasible; alternative schedulers require explicit documented
justification. Do not add scheduling infrastructure before a concrete need. Job handlers MUST
delegate business behavior, be idempotent when retries are possible, have behavioral tests, and
its first introduction requires an ADR. This avoids accidental scheduling infrastructure.

### XX. Repository Governance
Keep a stack-appropriate `.gitignore` that excludes generated, local, sensitive, and ephemeral
artifacts while retaining source, documentation, Spec Kit artifacts, Mermaid sources, ADRs, and
Maven Wrapper files. Commits MUST be cohesive, meaningful Conventional Commits and normally carry
their behavior's tests and documentation. `trunk` is the only permanent integration branch and
MUST remain buildable, testable, releasable, and linear; short-lived work integrates promptly
without merge commits or force-pushing `trunk`. Releases originate and are tagged from `trunk`.
The implementation guide is [`docs/development-workflow.md`](../../docs/development-workflow.md).

### XXI. Definition of Done
A feature is complete only when its acceptance criteria and all applicable constitutional principles
are satisfied; required tests are green; relevant migrations and documentation are included; no
known broken placeholders remain; the repository is clean; commits are cohesive Conventional
Commits; and `trunk` remains buildable, testable, and releasable. This prevents partial delivery
from being called complete.

## Product and Technical Constraints

- Business modules include `identity`, `household`, `catalog`, `shopping.list`,
  `shopping.input`, `shopping.item`, `shopping.history`, `collaboration`, and `preferences` when
  implemented. Names may change only for a business-capability boundary with an explicit API.
- Application code MUST remain framework-independent outside integration and presentation edges;
  domain code MUST NOT depend on application, infrastructure, UI, web, JPA, Spring, or Vaadin.
- CI MUST verify module boundaries and layer rules, run focused tests for changed modules, and run
  applicable full integration and end-to-end suites before merge.

## Delivery Workflow and Quality Gates

- Specifications, plans, tasks, implementation, review, and CI MUST comply with this constitution
  and the applicable supporting architecture guides.
- Each feature specification and task breakdown MUST identify its owning module, allowed
  dependencies and public interfaces, Use Cases, migration location, and required module,
  persistence, event, and end-to-end tests.
- Deviations require an explicit constitution amendment or an approved specification that narrows
  an allowed exception without contradicting this constitution.

## Governance

This constitution supersedes lower-priority engineering practices. Amendments MUST be documented
here with a rationale and semantic version impact. Use a MAJOR version for incompatible removals or
redefinitions, a MINOR version for new or materially expanded requirements, and a PATCH version
for clarifications or non-semantic documentation refactors. Compliance is reviewed during
specification, planning, task generation, implementation, code review, CI, and repository
administration; conflicting changes MUST be rejected or amended first.

**Version**: 1.4.1 | **Ratified**: 2026-08-29 | **Last Amended**: 2026-08-31
