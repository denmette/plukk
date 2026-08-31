# Testing

Testing proves observable behavior. It is organized by the behavior and boundary under test rather
than a fixed quota of test types.

## Test Levels

- Unit tests exercise domain and application behavior with real domain objects or focused fakes.
- Module integration tests use `@ApplicationModuleTest` to prove a module's exposed behavior and
  its domain-event interactions without unrelated business modules.
- Persistence integration tests exercise each module's adapter, Flyway migrations, and PostgreSQL
  using Testcontainers. H2 is not a substitute.
- End-to-end tests use Playwright against the running application and PostgreSQL for critical
  journeys. Use a representative mobile viewport and cover authentication, authorization,
  collaboration, and PWA or reconnection behavior where applicable.

End-to-end tests complement focused module tests and cannot be the only proof that a slice works.
CI runs module and architecture checks, focused tests for changed modules, and applicable full
integration and end-to-end suites before merge.

## Development and Test Identity

Production uses Authentik through OpenID Connect. Local development and automated tests may use an
explicit development or test identity mechanism so Authentik is not required for every run. Keep
normal Spring Security authorization and household or member checks active, isolate the mechanism
to development or test configuration, and ensure it cannot activate in production. Do not make
domain or Use Case code depend on Authentik-specific types or disable security globally.

## Java Test Style

Use JUnit and AssertJ. Tests have Given/When/Then structure and names in the form
`given<Precondition>_when<Action>_then<ExpectedBehavior>`.

```java
@Test
void givenBlankName_whenExecutingCreateUseCase_thenNotificationExplainsCorrection() {
    // Given
    // When
    // Then
}
```

Do not test trivial accessors, generated code, framework internals, or compiler-guaranteed behavior
unless Plukk adds behavior. Mockito is appropriate for focused doubles, but do not mock every
collaborator by default. Tests of expected validation or business failures assert the returned
Notification; tests of unexpected failures assert the exceptional behavior when relevant.
