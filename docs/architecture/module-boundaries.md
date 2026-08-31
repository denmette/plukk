# Module Boundaries

Plukk vertical slices are Spring Modulith application modules, not package conventions. A module
owns a business capability, its application behavior, persistence changes where applicable, and
tests.

## Declaring and Using Modules

Declare each module in its `package-info.java` with `@ApplicationModule`, explicit
`allowedDependencies`, and named interfaces for intentionally shared APIs. An undeclared dependency
is prohibited. A module may use another module only through its exposed API, named interface, or a
domain event; never through its internals, entities, repositories, adapters, UI, or Use Case
implementation.

```java
@ApplicationModule(
    allowedDependencies = {"identity :: api", "household :: api"}
)
package dev.casteels.plukk.shopping.list;
```

Business capability and independent evolution, rather than package aesthetics, determine a module
boundary. Shopping may evolve into distinct `shopping.list`, `shopping.input`, `shopping.item`, and
`shopping.history` modules when those capabilities warrant separate ownership; these are candidates,
not permanently mandated names.

## Verification and Focused Tests

Verify the application module graph with
`ApplicationModules.of(PlukkApplication.class).verify()`. Each module needs focused
`@ApplicationModuleTest` coverage of its exposed API and applicable emitted or consumed events.
Tests must not silently load unrelated business modules: every non-default dependency belongs in
`extraIncludes` and must also be declared as a module dependency.

`verifyAutomatically = false` is exceptional. Use it only with an adjacent explanation and never
as a workaround for a broken boundary. Module verification and focused module tests complement
end-to-end coverage; they are the evidence that a slice works independently.

Architecture rules additionally protect direction: domain code does not depend on application,
infrastructure, UI, web, JPA, Spring, or Vaadin; application code does not depend on infrastructure
implementations; UI and web code use public Use Cases or exposed module APIs.
