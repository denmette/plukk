<!--
Sync Impact Report
Version change: 1.1.0 -> 1.2.0
Modified principles:
- XVII. Definition of Done -> XVII. Definition of Done
Added sections:
- XX. Repository Governance
Removed sections:
- None
Follow-up TODOs:
- None
-->
# Plukk Constitution

## Core Principles

### I. Product Identity
Plukk is self-hosted software for one household per deployment. The project name is Plukk. The
Java base package MUST be `dev.casteels.plukk`, and all production Java packages MUST live under
that namespace. Placeholder package names such as `com.example` are prohibited. This principle
keeps the codebase and product scope aligned with the intended deployment model.

### II. Full-Java Architecture
Java MUST remain the primary application language. The backend and application UI MUST use Spring
Boot and Vaadin Flow. Separate SPA frontends such as React, Angular, or Vue are out of scope.
Client-side code is allowed only when required for browser capabilities such as PWA support,
service workers, or degraded connectivity behavior that Vaadin cannot reasonably deliver
server-side, and it MUST remain minimal and justified. This prevents stack sprawl and keeps the
operational model simple.

### III. Modular Monolith
Plukk MUST be implemented as a modular monolith using Spring Modulith. Microservices are out of
scope. Modules MUST represent business capabilities rather than technical layers, and their
boundaries MUST be verified with Spring Modulith tests. Cross-module dependencies MUST be
deliberate and minimal. Application events SHOULD be preferred when they materially reduce
coupling, but asynchronous complexity MUST NOT be introduced without a documented requirement. This
preserves maintainability without fragmenting the system prematurely.

### IV. Simple Deployment
Production MUST run as one stateless application container plus PostgreSQL. PostgreSQL MUST be
usable either as a local Docker container or as an externally hosted database. Runtime
configuration MUST come from environment variables or mounted configuration or secrets. Additional
infrastructure such as Redis, Kafka, RabbitMQ, Elasticsearch, or Kubernetes MUST NOT be added
without a concrete documented requirement. This keeps homelab deployment and operations practical.

### V. Modern Stable Technologies
Implementation MUST use the latest stable production releases available at the time work is done.
The preferred Java version MUST be the latest stable LTS release. Spring Boot, Spring Modulith,
Vaadin, PostgreSQL, Playwright, Testcontainers, Flyway, Maven plugins, and comparable
dependencies MUST use stable releases only. Alpha, beta, RC, milestone, snapshot, preview,
experimental, and canary dependencies are prohibited unless explicitly approved. Current versions
MUST be verified before implementation rather than recalled from memory, and major technology
versions MUST be documented in the README. This reduces avoidable upgrade and support risk.

### VI. Mobile-First User Experience
The primary user experience is a smartphone used one-handed while shopping. Design MUST begin from
approximately a 390px viewport, with larger screens treated as secondary enhancements. Core
shopping interactions MUST have large touch targets, require as few actions as reasonably
possible, and feel immediate when adding or checking items. Unpurchased items MUST remain visually
dominant, and purchased items MUST stay clearly distinguishable without disappearing automatically.
Accessibility, sufficient contrast, keyboard accessibility where relevant, and reduced-motion
preferences MUST be respected. This keeps the product optimized for its real usage context.

### VII. Vaadin Conventions
Standard Vaadin Flow components and patterns MUST be preferred over custom browser frameworks.
Views MUST stay small and be composed from focused components. A generic internal UI framework MUST
NOT be created prematurely. Business rules MUST NOT live primarily inside Vaadin views; views
coordinate presentation and application use cases while domain and application logic remain inside
the appropriate modules. This preserves clear separation without abandoning the chosen framework.

### VIII. PWA and Degraded Connectivity
Plukk MUST target installability as a Progressive Web App. It MUST provide a useful degraded
experience during temporary connectivity loss. A service worker MAY cache static assets and
appropriate read-only application data. The user MUST be told clearly when the application is
offline or disconnected, and the application MUST NOT imply that a write succeeded unless the
server confirmed persistence. Full offline mutation, conflict resolution, synchronization queues,
CRDTs, and similar distributed synchronization mechanisms are out of scope for the initial MVP
unless separately specified. This sets a reliable boundary for offline behavior.

### IX. Household Model
One Plukk installation represents one household. Multi-tenancy and multiple households per
installation are prohibited. Users MAY hold different household roles, and the domain model MUST
support at least owner, member, and guest concepts. Guest access MAY later be limited to selected
shopping lists. Complex invitation workflows are not required for the MVP unless explicitly
specified. This keeps the authorization model aligned with the single-household scope.

### X. Authentication and Authorization
Authentication MUST use an external Authentik installation through OpenID Connect. Plukk MUST NOT
manage user passwords. Spring Security MUST own authentication and authorization. Authentication
state MUST be handled securely using server-side and browser session mechanisms appropriate for
Vaadin and Spring Security. Authorization MUST be enforced server-side. Guests MUST NOT gain
access beyond their explicitly granted permissions. Security controls such as CSRF MUST NOT be
disabled for convenience, and secrets, tokens, session identifiers, and credentials MUST never be
logged or committed. This treats security boundaries as product requirements rather than
afterthoughts.

### XI. Persistence
PostgreSQL is the single source of truth for persistent application data. Database schema changes
MUST use Flyway. Production MUST NOT rely on Hibernate automatic schema creation. Persistence
entities MUST NOT become an accidental public API. Database design MUST remain suitable for backup
and restore using standard PostgreSQL tooling. This protects operational recoverability and schema
discipline.

### XII. Behavioral Testing Is Part of Every Feature
Tests are mandatory and MUST validate observable domain, application, integration-boundary, or
user behavior rather than implementation details. Production refactoring that preserves observable
behavior SHOULD NOT require unrelated test changes. Tests MUST NOT exist solely to increase code
coverage; coverage MAY be measured but is not evidence of correctness. Java tests MUST use a
Given/When/Then structure and the naming convention
`given<Precondition>_when<Action>_then<ExpectedBehavior>`; optional Given/When/Then comments MAY
be used when they improve readability. JUnit MUST be the test framework and AssertJ MUST be the
assertion library. Equivalent standard JUnit assertions SHOULD NOT be used in place of AssertJ.
Mockito MAY be used for appropriate doubles but MUST NOT mock every collaborator by default; real
domain objects and focused fakes are preferred where practical. This makes behavior, not internal
structure, the durable specification of the system.

Test scope MUST follow the behavior under validation rather than arbitrary layer quotas. Unit tests
SHOULD validate domain and application behavior. Integration tests MUST validate meaningful
boundaries such as persistence, security, application integration, and infrastructure integration.
PostgreSQL-dependent integration tests MUST use Testcontainers; H2 MUST NOT substitute for
PostgreSQL. Spring Modulith verification tests MUST validate module boundaries and other explicit
architectural constraints. End-to-end tests MUST use Playwright against the actual running
application and PostgreSQL; relevant journeys MUST include a mobile viewport representative of
Plukk's primary use. Authentication, authorization, shared-list collaboration, real-time behavior,
and PWA, degraded-connectivity, reconnection, or offline behavior MUST receive automated
behavioral coverage where applicable and technically practical. Tests MUST NOT target framework
internals, trivial accessors, generated code, or behavior adequately guaranteed by the compiler or
framework unless Plukk adds meaningful behavior around it. UnitSocializer MAY be used for unit-test
analysis but MUST NOT become a runtime dependency or a normal test-execution requirement.

### XIII. Vertical Slices
Features MUST be implemented as vertical slices. A slice normally includes domain behavior,
persistence, application or service behavior, UI, and automated tests. The team MUST NOT stage
work as all database tables first, then all services, then all UI pages. The application MUST
remain usable throughout development. This reduces integration risk and keeps progress user-visible.

### XIV. Domain Simplicity
A shopping product represents a reusable catalog entry, and a shopping item represents a product
placed on a shopping list. A shopping list MUST NOT accumulate duplicate active entries for the
same product by accident. Quantity and optional notes MUST cover common variations. Purchased items
MUST remain reversible so users can uncheck them. Inventory, recipes, meal planning, pricing,
supermarket integrations, receipt scanning, barcode scanning, and AI are out of scope unless
separately specified. This prevents domain sprawl in the MVP.

### XV. Scope Discipline
Implementation MUST cover only requirements present in the active specification. The team MUST NOT
design abstractions for hypothetical SaaS, multi-tenant, microservice, AI, analytics,
event-streaming, or enterprise requirements. Future extensibility is desirable only when it does
not introduce speculative engineering. This protects delivery speed and design clarity.

### XVI. Code Quality
Code organization MUST prefer package-by-feature and module-oriented structure. Generic
`BaseController`, `BaseService`, `BaseRepository`, and similar inheritance hierarchies are
prohibited unless a concrete case is documented and approved. Interfaces MUST NOT be created only
because an implementation class exists. Java code MUST favor explicit readability. Lombok MUST NOT
be introduced without a concrete justification. Records SHOULD be used for immutable DTOs and
value objects where appropriate. Spring and Vaadin conventions MUST be followed before introducing
custom abstractions. This keeps the codebase legible and intention-revealing.

### XVII. Definition of Done
A feature is done only when production code compiles; acceptance criteria are satisfied; required
behavioral tests pass and follow the Given/When/Then naming and structure convention; AssertJ is
used for Java assertions; relevant integration tests use Testcontainers; relevant Playwright
end-to-end tests and Spring Modulith architecture checks pass; mobile usability, authentication,
and authorization rules are respected; database migrations are included when required; and no
knowingly broken placeholders or TODO implementations remain. Documentation affected by the
feature MUST be updated, including diagrams when architectural relationships, flows, states, or
deployment topology change, and significant architectural decisions MUST be recorded when
required. Relevant source files MUST be tracked; generated, local-only, sensitive, and ephemeral
files MUST be ignored; and secrets or local credentials MUST NOT be committed. Commits MUST be
cohesive, meaningful Conventional Commits, and integration MUST preserve a buildable, testable,
releasable, linear `trunk` history with obsolete short-lived branches removed. This definition
prevents partial delivery from being treated as completion.

### XVIII. Documentation Is Part of the Product
Useful documentation MUST evolve in the same vertical slice as the implementation and MUST serve a
concrete audience and purpose; documentation created merely to satisfy a requirement is prohibited.
Stale documentation is a defect. The README MUST remain concise and act as the project entry point;
detailed material MUST live in an appropriate documentation structure. The project MUST document,
where applicable, its purpose, developer setup and local development, architecture and module
boundaries, domain concepts, Authentik integration, configuration, PostgreSQL and Flyway,
testing, container deployment, Docker Compose, external PostgreSQL, backup and restore, PWA and
degraded connectivity, operational troubleshooting, and non-obvious architectural decisions.

Long or complex technical documentation MUST include diagrams or images where they materially
improve comprehension. Mermaid MUST be used for diagrams that it can reasonably express, with the
source embedded in the relevant version-controlled Markdown; exported diagram images MUST NOT be
the source of truth in those cases. Diagrams MUST add information rather than duplicate prose, and
complex diagrams SHOULD be split into focused views. The Mermaid diagram type MUST suit the
information being conveyed, including flowchart, sequence, state, class, entity-relationship, C4,
Git graph, or timeline diagrams where appropriate. Architecture documentation MUST explain the
major Spring Modulith modules, their responsibilities and allowed dependencies, important
interactions and domain events, and runtime/deployment topology, using Mermaid where appropriate;
it MUST describe intent and constraints rather than duplicate every class. Significant decisions
whose rationale is not evident from the implementation SHOULD use lightweight ADRs that explain
context, decision, consequences, and relevant alternatives.

### XIX. Background Jobs and Scheduling
When persistent background processing, delayed work, retries, or recurring business work is
required, JobRunr MUST be the preferred solution. Spring `@Scheduled`, custom executor schedulers,
Quartz, and similar alternatives MUST NOT be introduced for persistent or business-relevant work
without explicit documented justification. JobRunr SHOULD use PostgreSQL persistence where feasible
to avoid additional infrastructure, and it MUST NOT be added before a concrete requirement exists.
Job handlers MUST invoke application or domain behavior rather than contain substantial business
logic, MUST be idempotent when retries can occur, and MUST have automated behavioral tests. The
first introduction of JobRunr MUST be documented as an architectural decision.

### XX. Repository Governance
The repository MUST contain an appropriate `.gitignore` and keep it current for the actual Plukk
toolchain. It MUST ignore generated build output and dependencies, Vaadin-generated frontend and
build artifacts, IDE-local state, machine-specific files, Playwright output, test reports, logs,
temporary files, local container or database data, local environment overrides, credentials, and
secrets. Example configuration MAY be version-controlled only without real secrets. Specifications,
plans, tasks, documentation, Mermaid sources, ADRs, Maven Wrapper files, and project source files
MUST remain version-controlled. The `.gitignore` MUST NOT accumulate unrelated tool patterns.

Every commit MUST follow Conventional Commits in the form
`<type>(optional-scope): <description>`. Allowed types include `feat`, `fix`, `test`, `docs`,
`refactor`, `build`, `ci`, `chore`, and `perf`; breaking changes MUST use Conventional Commits
breaking-change notation. Descriptions MUST be concise, imperative, and meaningful; vague messages
such as `changes`, `update`, `fix stuff`, `wip`, or `more work` are prohibited. Commits MUST be
cohesive and independently understandable, avoiding both unrelated giant changes and trivial
commit-count inflation. Behavior changes MUST normally include their intrinsic tests and
documentation.

`trunk` is Plukk's sole permanent and authoritative integration branch. `main`, `master`,
`develop`, `development`, and `integration` MUST NOT serve as alternative permanent integration
branches. Documentation, automation, CI, release processes, and repository settings MUST treat
`trunk` as primary, and it MUST remain buildable, testable, and releasable. Development follows
trunk-based development: sufficiently small safe changes MAY be made directly on `trunk`; otherwise
short-lived branches MUST originate from, synchronize with, and integrate quickly into `trunk`.
Long-lived feature branches and alternative integration branches are prohibited. Incomplete code
MAY reach `trunk` only when it is safe, not unintentionally exposed, and passes all quality gates.
Feature toggles require a concrete need.

The `trunk` history MUST remain linear. Merge commits MUST NOT enter `trunk`; branches MUST rebase
onto current `trunk` when necessary and integrate by rebase-and-merge or, when intermediate commits
do not warrant preservation, squash-and-merge. Published `trunk` history MUST NOT be rewritten and
force-pushing `trunk` is prohibited. Force-pushing shared branches SHOULD be avoided and requires
coordination before history is rewritten. Pull requests MAY support review and validation but MUST
be small, short-lived, based on `trunk`, pass applicable gates, use a linear integration strategy,
and have their source branch deleted after integration. Hosting platforms SHOULD protect `trunk` by
prohibiting force pushes and merge commits, requiring linear history and applicable status checks,
and allowing rebase merges and optionally squash merges.

Releases MUST originate from `trunk`, and release tags MUST point to commits already on `trunk`.
Permanent release branches are prohibited; exceptional temporary release branches MUST be
short-lived. Version tags SHOULD use a consistent semantic-version format such as `v1.0.0`. Spec
Kit artifacts follow this same policy: its generated feature branch is a short-lived branch from
`trunk`, not a permanent branch model, and MUST ultimately be rebased and integrated according to
the linear-history policy.

## Product and Technical Constraints

- Plukk MUST remain a self-hosted, single-household application rather than evolve implicitly into
  a multi-tenant SaaS platform.
- Likely business modules include `identity`, `household`, `catalog`, `shopping`,
  `collaboration`, and `preferences`. These names are guidance and MAY be refined during design,
  but the capability boundaries MUST remain business-oriented.
- Production infrastructure MUST stay limited to the application container and PostgreSQL unless a
  new requirement explicitly justifies expansion.
- Major technology versions in active use MUST be recorded in the README so implementation and
  review work share the same baseline.
- JobRunr is permitted only for a concrete persistent background-processing or scheduling need and
  MUST use PostgreSQL persistence where feasible.
- The repository MUST maintain a stack-appropriate `.gitignore`; source, documentation, Spec Kit
  artifacts, and Maven Wrapper files MUST be tracked, while local, generated, sensitive, and
  ephemeral artifacts MUST be ignored.

## Delivery Workflow and Quality Gates

- Every specification, plan, task list, implementation, and code review MUST treat this
  constitution as the highest-priority engineering policy.
- New work MUST be expressed and reviewed as vertical slices that preserve a usable application
  state.
- Reviews MUST verify module boundaries, mobile-first usability, security rules, persistence
  discipline, behavioral test expectations, and documentation quality from this constitution.
- Documentation reviews MUST verify that Markdown contains Mermaid source for diagrams where
  visuals materially improve technical understanding, and that significant decisions have ADRs.
- Reviews and automation MUST verify Conventional Commit messages, repository hygiene, `trunk` as
  the sole permanent integration branch, trunk-based development, linear integration, and release
  tags originating from `trunk` where applicable.
- Deviations from the constitution require an explicit documented amendment or an approved
  specification that narrows an allowed exception without contradicting the constitution.

## Governance

This constitution supersedes lower-priority engineering practices for Plukk. Amendments MUST be
documented in `.specify/memory/constitution.md`, include a clear rationale, and record the semantic
version impact. A MAJOR version bump is required for incompatible removals or redefinitions of
governance. A MINOR version bump is required for new principles or materially expanded guidance. A
PATCH version bump is required for clarifications and non-semantic wording changes. Compliance MUST
be checked during specification, planning, task generation, implementation, code review, and
repository administration. Changes that conflict with this constitution MUST be rejected or
accompanied by a constitution amendment applied first.

**Version**: 1.2.0 | **Ratified**: 2026-08-29 | **Last Amended**: 2026-08-30
