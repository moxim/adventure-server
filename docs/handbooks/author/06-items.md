# 6. Items

Items are the things in your world a player can look at — and often pick
up, carry, drop, or wear.

## Two ways to see your items

- **Manage Items** on the Adventure Editor opens **every item in the
  adventure**, across all locations, with a **Location** column so you can
  see where each one lives. It also has a **Create in Location** picker —
  choose the location first, then create the item into it.
- **Manage Items** on a *location* editor (via **Manage Locations** →
  select a location) opens just that location's items.

Either grid shows **Adjective**, **Noun**, **Short Description**, and
Yes/No columns for **Containable**, **Wearable**, and **Worn**.

## The item editor

| Field | Notes |
|-------|-------|
| **Adjective** | Optional, from your vocabulary. |
| **Noun** | Required, from your vocabulary. |
| **Short description** / **Long description** | What a player reads when they glance at, or examine, the item. |
| **Can be picked up / Is containable** | Check this to let players `take` it. |
| **Is wearable** | Check this to let players `wear` it. |
| **Is worn** | Whether it starts worn. |
| **Item ID** / **Location ID** / **Adventure ID** | Read-only identifiers. |

**Manage Commands** on this screen takes you to the item's own commands —
same [command editor](08-commands-actions-and-conditions.md) used
everywhere else. It's disabled until you've saved the item at least once —
commands attach to the saved item, so there's nothing to attach them to yet
on a brand-new, unsaved one.

> **Note:** There's no "parent container" picker on this screen. An item
> always belongs to the location you created it under — if you need it
> somewhere else, create it there instead, or move it at runtime with a
> **Move Item** action (see the
> [Action reference](appendix-a-action-and-condition-reference.md)).

## Checking "Can be picked up"

Checking **Containable** doesn't just flag the item — it generates the
`take` and `drop` commands for it automatically, so you don't have to build
those by hand for every pickable item.

> **Note:** This depends on your adventure already having its **Taker**
> and **Dropper** special words set (see
> [Chapter 7](07-vocabulary-and-words.md#special-words)). If they aren't
> set yet, you'll see: *"Please select verbs to allow a player to handle
> this item in the vocabulary section."* — and the checkbox reverts to
> unchecked. Set your special words first, then come back.
>
> Unchecking it again removes exactly the take/drop commands it added —
> anything else you've built for this item is left alone.

## What's next

Items only feel real once your vocabulary has words to describe them with —
[Chapter 7](07-vocabulary-and-words.md) covers vocabulary next.
