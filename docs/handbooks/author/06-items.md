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

Checking **Can be picked up / Is containable** only flags the item as
something that can be carried and put in containers. It does **not** create
any `take` or `drop` commands for the item.

To let players actually pick things up and put them down, add a few
**Responses** once for the whole adventure, using the wildcard noun `~`
(*"any item"*) and the automatic actions **AUTOT**, **AUTOD**, **AUTOW** and
**AUTOR** — see
[Chapter 9](09-workflow.md#one-response-for-every-item-the-wildcard-noun) and
the [Action reference](appendix-a-action-and-condition-reference.md).

> **Note:** If you built an adventure with an earlier version, its items may
> still carry old, hand-generated `take`/`drop` commands. Those are ordinary
> item commands and keep working, and because item commands are tried before
> Responses, they win over the wildcard Responses for that item. Delete them
> in **Manage Commands** if you'd rather have the Responses handle it.

## What's next

Items only feel real once your vocabulary has words to describe them with —
[Chapter 7](07-vocabulary-and-words.md) covers vocabulary next.
