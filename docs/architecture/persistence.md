# Persistence

PostgreSQL is Plukk's authoritative persistent store. Flyway governs schema evolution; production
does not use Hibernate automatic schema creation.

## Module Ownership

Every migration has one module owner and lives in that module's dedicated Flyway location. The same
module owns the affected tables and persistence adapter. Do not create shared table ownership or
cross-module foreign keys without a documented architecture exception.

The default is one application database and one Flyway configuration. Schema-per-module isolation
or independent module deployment needs an ADR because it changes the operational model.

## Integration Verification

Each module's persistence adapter has a PostgreSQL Testcontainers integration test that applies its
Flyway migrations and exercises meaningful behavior. This verifies SQL, migration ordering, and
database semantics against PostgreSQL rather than H2.

Design data for backup and restore using standard PostgreSQL tooling. Treat persistence entities as
internal implementation details, not as a public module API.
