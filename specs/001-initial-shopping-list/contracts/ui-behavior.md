# Contract: Shopping UI Behavior

## Audience and Scope

This contract describes observable behavior for household members using Plukk on a phone. It
excludes visual implementation details while defining states for acceptance and end-to-end tests.

## List Behavior

| Situation | Required outcome |
|---|---|
| Open a list | Show items grouped by category, with active items visually dominant. |
| Purchase an item | Keep it visible in a distinct purchased state. |
| Restore an item | Return it to active state. |
| Remove an item | Remove it and publish the confirmed result to active members. |
| Shared change | Show a confirmed change to other active members within the success target. |
| Concurrent same-item change | Show the latest confirmed state to all active members. |

## Connectivity Behavior

| State | Required outcome |
|---|---|
| Connected | Normal interaction and confirmation feedback are available. |
| Disconnected | Clearly indicate the state and keep already visible information useful where possible. |
| Confirmation interrupted | Do not mark the change saved; show a clear unresolved or failed state. |
| Reconnected | Clearly show recovery and refresh the list to the latest confirmed state. |

## Authorization Behavior

- An unauthenticated visitor is directed to sign in through the configured external provider.
- An identity without active household-member access cannot access household data.
- First release exposes member behavior only; guest invitations and per-list guest access are absent.
