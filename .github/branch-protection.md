# `trunk` Branch Protection

Apply these repository settings to `trunk`:

- Require pull requests before merging unless the change is a deliberately small direct-to-trunk
  update by a maintainer.
- Require status checks to pass before merging.
- Required checks:
  - `Verify / verify`
- Require branches to be up to date before merging.
- Allow rebase merge and squash merge only.
- Disable merge commits.
- Prevent force pushes.
- Prevent branch deletion.
- Require linear history.
- Require conversation resolution before merging.
- Require signed commits if the repository policy mandates them.

`trunk` is the only permanent integration branch. Repository automation, release tagging, and
documentation should treat `trunk` as authoritative.
