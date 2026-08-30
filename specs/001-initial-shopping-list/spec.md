# Feature Specification: Initial Shopping List

**Feature Branch**: `001-initial-shopping-list`

**Created**: 2026-08-29

**Status**: Draft

**Input**: User description: "Initial product specification for Plukk's first useful release."

## Clarifications

### Session 2026-08-29

- Q: When two household members change the same shopping item at nearly the same time, which outcome should Plukk use? → A: Latest confirmed change wins; both members see the resulting state.
- Q: When a member adds a shopping need that exactly matches an active item already on the list, what should happen? → A: Keep the existing item unchanged and bring it into view.
- Q: How should product categories work in the first release? → A: Fixed starter categories; members select one for custom products.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Add a Shopping Need Quickly (Priority: P1)

As a household member shopping on a phone, I want to add a concrete shopping need with a short
piece of text so that I can keep the list up to date with minimal effort.

**Why this priority**: Fast addition is the core product value and makes a list useful before any
advanced catalog, history, or collaboration behavior exists.

**Independent Test**: A signed-in household member can create a list, enter a valid shopping need,
and see the resulting active item in that list.

**Acceptance Scenarios**:

1. **Given** a household member has an empty shopping list, **When** they enter `kipfilet 400g`,
   **Then** the list shows an active item based on catalog product `Kip` with variant `Kipfilet`,
   quantity `400`, and unit `gram`.
2. **Given** a household member has a shopping list, **When** they enter `melk 2x1l`, **Then** the
   list shows an active item based on catalog product `Melk` with quantity `2` and package size
   `1 liter`.
3. **Given** a household member has entered a shopping need, **When** its product is absent from
   the catalog, **Then** they can create a local custom product and add the resulting item.
4. **Given** a household member enters input that cannot be interpreted reliably, **When** they
   submit it, **Then** no item is added and they receive understandable feedback with a useful
   example for reformulating the input.

---

### User Story 2 - Shop From a Clear List (Priority: P1)

As a household member in a store, I want active items organized for quick recognition and able to
be marked purchased or restored so that I can complete a trip without losing context.

**Why this priority**: A shopping list only delivers its intended value when it remains legible and
easy to operate during an active shopping trip.

**Independent Test**: A member can add items to a list, identify active items, mark one purchased,
and restore it without the item disappearing.

**Acceptance Scenarios**:

1. **Given** a list contains active items in different categories, **When** the member views the
   list, **Then** items are visually organized by category and active items are more prominent than
   purchased items.
2. **Given** a list contains an active item, **When** the member marks it purchased, **Then** the
   item remains visible in a purchased state.
3. **Given** a list contains a purchased item, **When** the member unmarks it, **Then** it returns
   to the active state.
4. **Given** a list already contains an active concrete shopping need, **When** the member adds
   the same concrete need again, **Then** the list keeps the existing item unchanged and brings it
   into view rather than creating a duplicate.

---

### User Story 3 - Manage Household Shopping Lists (Priority: P1)

As a household member, I want to create and manage multiple shopping lists so that separate trips
or purposes can be planned independently.

**Why this priority**: Households commonly need more than one list, and list management is needed
to make the core shopping flow practical.

**Independent Test**: A member can create two lists, rename one, open either, and delete one while
the other remains available.

**Acceptance Scenarios**:

1. **Given** a signed-in member, **When** they create a shopping list with a name, **Then** the
   list appears among that household's available lists.
2. **Given** a household has a shopping list, **When** a member renames it, **Then** all household
   members see the new name.
3. **Given** a household has two lists, **When** a member deletes one, **Then** only the selected
   list and its items are removed.

---

### User Story 4 - Reuse Products and Recent Needs (Priority: P2)

As a household member, I want to find familiar products and re-add a recently purchased concrete
need so that routine shopping takes less typing.

**Why this priority**: Reuse accelerates the main flow while building naturally on completed
shopping activity.

**Independent Test**: A member can search the starter or custom catalog, purchase a concrete item,
and add the same concrete item again from recently used needs.

**Acceptance Scenarios**:

1. **Given** the catalog contains common products, **When** a member searches by product name,
   **Then** matching products are discoverable with their category and visual recognition aid.
2. **Given** a member previously purchased `Kipfilet - 400 g`, **When** they choose it from recent
   needs, **Then** a new active item preserves the variant, quantity, and unit.
3. **Given** a member creates a custom product, **When** another household member searches the
   catalog, **Then** that product is available within the same installation.
4. **Given** a member creates a custom product, **When** they choose its category, **Then** they
   select one from the fixed starter category list.

---

### User Story 5 - Collaborate Safely During Shopping (Priority: P2)

As a household member, I want changes made by another active member to appear promptly, and I want
connectivity problems explained clearly, so that a shared list remains trustworthy in a store.

**Why this priority**: Shared, dependable use is a key differentiator, but it builds on the core
list and item flow.

**Independent Test**: Two signed-in household members view the same list; a change by one becomes
visible to the other, while an interrupted connection is clearly indicated and unconfirmed writes
are not shown as saved.

**Acceptance Scenarios**:

1. **Given** two active household members are viewing the same list, **When** one adds, purchases,
   restores, removes, or changes an item, **Then** the other sees the change promptly.
2. **Given** a member loses connectivity while viewing a list, **When** the connection state
   changes, **Then** the application clearly indicates that state and keeps already visible
   information useful where possible.
3. **Given** a member attempts a change before it is confirmed as stored, **When** connectivity is
   interrupted, **Then** the application does not present that change as successfully saved.
4. **Given** connectivity returns after a temporary interruption, **When** the member resumes use,
   **Then** the list recovers gracefully without requiring the member to infer its connection state.
5. **Given** two members change the same shopping item at nearly the same time, **When** both
   changes are confirmed, **Then** the latest confirmed change is the item state shown to both
   members.

### Edge Cases

- A member enters a quantity without a unit, such as `appels 6`; the item retains the count while
  leaving the unit unspecified.
- A member enters package wording such as `cola 2 flessen` or `kaas 2 pakken`; the item preserves
  the recognized quantity and package description.
- A member attempts to add input with conflicting or ambiguous quantities; no incorrect item is
  created and the member receives reformulation guidance.
- A member removes an item while another member marks it purchased; the result remains internally
  consistent and becomes visible to both members.
- The catalog search has no match; the member can create a custom product rather than being blocked.
- A user who is not a household member cannot view or change household lists or catalog data.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow authenticated household members to create, open, rename, and
  delete multiple shopping lists for their household.
- **FR-002**: The system MUST limit first-release shopping-list access to normal household members
  while retaining owner, member, and guest roles as future domain concepts.
- **FR-003**: The system MUST provide a reusable, searchable catalog of common household and
  grocery products, each with a name, category from the fixed starter category list, and visual
  recognition aid.
- **FR-004**: The system MUST allow members to create custom catalog products local to their
  household installation when a desired product is absent and MUST require selection of a fixed
  starter category for each custom product.
- **FR-005**: The system MUST distinguish a reusable catalog product from a concrete shopping-list
  item, including a variant or description, quantity, unit, and package information where supplied.
- **FR-006**: The system MUST let a member add a concrete shopping need through one concise
  free-text interaction, including common quantity, unit, multiplier, package-size, and variant
  expressions.
- **FR-007**: The system MUST interpret supported examples including `kipfilet 400g`, `melk 2x1l`,
  `appels 6`, and `water 6x1.5l` into the applicable concrete shopping-item information.
- **FR-008**: The system MUST reject ambiguous or unsupported free-text input without creating an
  incorrect item and MUST give immediate, understandable reformulation feedback.
- **FR-009**: The system MUST treat normal unparseable input as user feedback rather than an
  unexpected application failure, while making unexpected failures observable to the operator.
- **FR-010**: The system MUST organize shopping-list items by product category and make active
  items visually more prominent than purchased items.
- **FR-011**: The system MUST allow members to mark, unmark, and remove shopping-list items;
  purchased items MUST remain visible until explicitly removed or otherwise changed by a member.
- **FR-012**: The system MUST prevent duplicate active items representing the same concrete
  shopping need on the same list; when a member adds an exact active match, the system MUST keep
  the existing item unchanged and bring it into view.
- **FR-013**: The system MUST retain purchased concrete shopping needs with their useful variant,
  quantity, and unit information and make them available for quick re-addition.
- **FR-014**: The system MUST make meaningful changes to a shared list visible promptly to other
  active household members, including add, purchase, restore, removal, and item-information
  changes.
- **FR-015**: When household members make concurrent changes to the same shopping item, the system
  MUST use the latest confirmed change as the resulting item state and show that state to both
  members.
- **FR-016**: The system MUST optimize primary shopping flows for one-handed smartphone use with
  clear touch targets, quick search and add interactions, and low cognitive load.
- **FR-017**: The system MUST be installable for convenient mobile access and MUST clearly indicate
  temporary loss of connectivity or reconnection.
- **FR-018**: During temporary connectivity loss, the system MUST keep already visible shopping
  information useful where reasonably possible and MUST NOT present an unconfirmed change as saved.
- **FR-019**: The system MUST NOT provide offline item creation, mutation queues, conflict
  resolution, full offline shopping, guest invitations, fine-grained guest access, automated
  recurring-product suggestions, advanced shopping-history analysis, or unrelated product domains
  in this release.

### Key Entities *(include if feature involves data)*

- **Household**: The single group represented by one Plukk installation; it owns members, shopping
  lists, products, categories, and shopping history.
- **Household User**: A person authenticated to use the household, with a role concept of owner,
  member, or guest.
- **Shopping List**: A named collection of active and purchased concrete shopping items.
- **Catalog Product**: A reusable general product, such as `Kip`, with a category and visual
  recognition aid.
- **Shopping Item**: A concrete need on one shopping list, such as `Kipfilet - 400 g`, linked to a
  catalog product and holding applicable variant, quantity, unit, package, and purchase state.
- **Category**: A fixed starter shopping grouping used to organize catalog products and list items.
- **Shopping History Entry**: A record of a purchased concrete shopping need used to enable recent
  re-addition and future non-predictive reuse features.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In moderated household trials, at least 90% of members can add a supported shopping
  need and see it on the intended list within 15 seconds on a smartphone.
- **SC-002**: At least 90% of supported quantity and package examples in the acceptance suite are
  interpreted into the intended concrete shopping need without correction.
- **SC-003**: In a shared-list trial, changes made by one active household member are visible to a
  second active member within 3 seconds in at least 95% of observed cases under normal connectivity.
- **SC-004**: At least 90% of members can mark and restore a purchased item on a smartphone without
  assistance during first use.
- **SC-005**: In temporary connectivity-interruption tests, 100% of attempted unconfirmed changes
  are visibly distinguished from successfully stored changes.
- **SC-006**: At least 80% of members can re-add a recently purchased concrete shopping need within
  10 seconds without re-entering its variant, quantity, or unit.

## Assumptions

- Each deployment starts with one household and its first normal member can use its configured
  external identity; household onboarding and guest invitation flows are out of scope.
- The first-release starter catalog contains common household and grocery products suitable for the
  intended household, while members can add custom products for gaps.
- The initial category taxonomy uses the examples supplied in the product description. Members can
  select from that fixed starter list for custom products; category administration is deferred.
- Recently used needs are shown from previously purchased items; favorites, prediction, and
  automatic recurring suggestions are deferred.
- Concurrent changes to the same shopping item use the latest confirmed change; sophisticated
  distributed conflict resolution is not required.
- Temporary degraded connectivity covers clear status, graceful recovery, and useful already shown
  information, not offline mutations or synchronization.
