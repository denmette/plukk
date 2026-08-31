# Contract: Shopping Input Interpretation

## Purpose

Define the observable contract between the mobile add interaction and shopping behavior. It is not
a public HTTP API contract.

## Input

An authorized household owner or member supplies one non-blank text value. Supported examples include:

| Input | Expected interpretation |
|---|---|
| `kipfilet 400g` | Product `Kip`; variant `Kipfilet`; quantity `400`; unit `gram`. |
| `kippenblokjes 200 g` | Product `Kip`; variant `Kippenblokjes`; quantity `200`; unit `gram`. |
| `appels 6` | Product `Appels`; quantity `6`; no unit. |
| `melk 2x1l` | Product `Melk`; quantity `2`; package size `1 liter`. |
| `cola 2 flessen` | Product `Cola`; quantity `2`; package descriptor `flessen`. |
| `water 6x1.5l` | Product `Water`; quantity `6`; package size `1.5 liter`. |

## Outcomes

| Outcome | Required behavior |
|---|---|
| Interpreted | Show a reviewable concrete need and allow confirmed addition to the selected list. |
| Exact active match | Do not create or modify an item; bring the existing item into view. |
| No catalog match | Offer local custom-product creation with a fixed starter category. |
| Ambiguous or unsupported | Add no item; give immediate plain-language feedback and a reformulation example. |
| Unexpected failure | Add no item; show safe feedback and make the failure observable to the operator. |

## Invariants

- The interaction never creates an item from an uncertain interpretation.
- A normal interpretation failure is user feedback, not an incident message.
- A successful addition is shown only after storage confirmation.
