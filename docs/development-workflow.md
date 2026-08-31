# Development Workflow

The [constitution](../.specify/memory/constitution.md) makes `trunk` the only permanent integration
branch. This guide describes the repository practices that implement that policy.

## Branch and Review Flow

1. Start a short-lived branch from current `trunk` unless the change is small and safe enough for
   direct integration.
2. Rebase onto current `trunk` before review and again before integration when necessary.
3. Keep pull requests focused, pass applicable CI checks, and integrate with rebase-and-merge or
   squash-and-merge. Merge commits must not enter `trunk`.
4. Delete the source branch after integration. Do not force-push published `trunk`; avoid
   force-pushing shared branches without coordination.

## Commit and Repository Rules

Use Conventional Commits: `<type>(optional-scope): description`. Valid types are `feat`, `fix`,
`test`, `docs`, `refactor`, `build`, `ci`, `chore`, and `perf`; breaking changes use the standard
breaking-change notation. Make commits independently understandable and cohesive. Behavior changes
normally include their tests and documentation.

Keep `.gitignore` specific to the Plukk toolchain. Ignore generated build and Vaadin assets, IDE
and machine state, Playwright output, test reports, logs, temporary files, local database data,
environment overrides, credentials, and secrets. Track source, documentation, Spec Kit artifacts,
Mermaid source, ADRs, and Maven Wrapper files.

## Platform Settings and Releases

Hosting should protect `trunk` with applicable status checks, linear history, no force pushes, and
no merge commits. Release tags point to commits already on `trunk`; use consistent semantic tags
such as `v1.0.0`. Permanent release branches and alternate integration branches are not allowed.

```mermaid
gitGraph
    commit id: "trunk"
    branch docs/governance
    checkout docs/governance
    commit id: "docs: guide changes"
    checkout trunk
    commit id: "rebase or squash integration"
    commit id: "release tag"
```
