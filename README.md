# Plukk

Plukk is a self-hosted shopping-list application for one household per deployment. The product is
planned as a Spring Boot and Vaadin Flow modular monolith with PostgreSQL persistence.

## Verified Build Baseline

As of 2026-08-30, the repository is bootstrapped with these verified stable build-tool versions:

- Java 25 LTS as the target JDK baseline from the active specification
- Apache Maven 3.9.16 via Maven Wrapper
- Apache Maven Wrapper 3.3.4 using the `only-script` distribution type

Use `./mvnw -v` to confirm the local wrapper installation and `./mvnw <goal>` for future project
builds once a JDK is installed locally.
