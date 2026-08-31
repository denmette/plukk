# Contract: Shopping UI Behavior

## Audience and Scope

This contract describes observable behavior for authorized household owners and members using Plukk on a phone. It
excludes visual implementation details while defining states for acceptance and end-to-end tests.

## List Behavior

| Situation | Required outcome |
|---|---|
| Open a list | Show items grouped by category in category display order, with active items visually dominant. |
| Purchase an item | Keep it visible in a distinct purchased state; touch target labeled "Purchase". |
| Restore an item | Return it to active state; touch target labeled "Restore". |
| Remove an item | Remove it and publish the confirmed result to active members; touch target labeled "Remove". |
| Restoring would duplicate an active item | Reject with Notification feedback rather than creating a second active item. |
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
- An identity without active `OWNER` or `MEMBER` household access cannot access household shopping data.
- Guests cannot access shopping lists in this release; guest invitations and per-list guest access are absent.
