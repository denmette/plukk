# Development Workflow

Plukk uses `trunk` as its only permanent integration branch. Work either lands directly on
`trunk` when it is small and safe, or on a short-lived branch that is rebased onto current
`trunk` before integration.

## Commit Rules

- Use Conventional Commits in the format `<type>(optional-scope): description`.
- Allowed types: `feat`, `fix`, `test`, `docs`, `refactor`, `build`, `ci`, `chore`, `perf`.
- Keep commits cohesive: behavior changes travel with their tests and documentation.

## Branch Rules

- Create short-lived branches from `trunk`.
- Rebase regularly onto the latest `trunk`.
- Integrate with rebase-and-merge or squash-and-merge only.
- Delete the source branch after integration.
- Do not merge `main`, `master`, `develop`, or other long-lived integration branches into the flow.

## Release Rules

- Tag releases from `trunk`.
- Keep `trunk` buildable, testable, and releasable at all times.
- Reject merge commits and force pushes on `trunk`.

## Repository Graph

```mermaid
gitGraph
    commit id: "bootstrap"
    branch feat/lists
    checkout feat/lists
    commit id: "feat(shopping): manage lists"
    commit id: "test(shopping): cover list flow"
    checkout trunk
    merge feat/lists id: "squash or rebase"
    commit id: "release prep"
```
