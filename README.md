# Plukk

Plukk is a self-hosted shopping-list application for one household per deployment. The product is
planned as a Spring Boot and Vaadin Flow modular monolith with PostgreSQL persistence.

## Verified Build Baseline

As of 2026-08-30, the repository is bootstrapped with these verified stable build-tool versions:

- Java 25 LTS as the target JDK baseline from the active specification
- Apache Maven 3.9.16 via Maven Wrapper
- Apache Maven Wrapper 3.3.4 using the `only-script` distribution type
- Spring Boot 4.1.1
- Vaadin 25.2.6
- Spring Modulith 2.1.1
- Testcontainers 2.0.5
- Playwright for Java 1.60.0
- PostgreSQL JDBC 42.7.13

Use `./mvnw -v` to confirm the local wrapper installation and `./mvnw <goal>` for future project
builds once a JDK is installed locally.

## Entry Point

The application entry point is `dev.casteels.plukk.PlukkApplication` in
`src/main/java/dev/casteels/plukk/PlukkApplication.java`. It boots the Spring Boot application and
declares the initial Vaadin PWA metadata for the household shopping experience.

## Developer Prerequisites

- Java 25
- Docker for PostgreSQL and Testcontainers-backed integration tests
- Access to an Authentik OIDC provider for authenticated flows
- Playwright browser binaries for end-to-end coverage

## Local Run

Docker Compose support is intentional. Starting the application from an IDE or with
`./mvnw spring-boot:run` starts the PostgreSQL 17 service in `compose.yaml`, waits for its health
check, and keeps it running after the application stops. Set the `PLUKK_DB_*` and `PLUKK_OIDC_*`
environment variables when using an external PostgreSQL instance or Authentik provider.

Run `./mvnw verify` with Docker running to execute the PostgreSQL Testcontainers integration suite
and Spring Modulith architecture verification.
