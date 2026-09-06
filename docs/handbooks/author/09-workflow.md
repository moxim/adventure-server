# 9. Workflow: Processes & Responses

Everything in [Chapter 8](08-commands-actions-and-conditions.md) is scoped
to one location or item. **Workflow** commands are the opposite: they apply
no matter where the player is. There are two kinds, and they behave
differently:

| Kind | When it runs | If its preconditions aren't met |
|------|--------------|--------------------------------|
| **Process** | Automatically, **every turn**, before location commands | Still runs — and any message it's set up to show appears **every turn** |
| **Response** | Only when the player types a command that **matches it exactly** | Falls through **silently** to normal handling — nothing is shown |

Both are reached from the [Adventure Editor](04-the-adventure-editor.md):
**Manage Processes** and **Manage Responses**. Both screens look and work
the same way — a grid of commands on top, a single-command editor below,
with **Back**, **New**, **Delete** (asks for confirmation), and **Save**
buttons. Selecting a grid row loads it into the editor; the editor itself is
the exact same **Verb / Adjective / Noun** pickers and **Preconditions &
Actions** editor from [Chapter 8](08-commands-actions-and-conditions.md) —
same Add/Up/Down/Remove controls, same **Negate** checkbox, same rules.
**Save** commits the loaded command and resets the editor to a blank new
one.

There's no Command Chain concept in either screen — a Workflow command
doesn't share a verb/adjective/noun with variants the way a location command
can. Each one stands alone.

## Processes: run every turn

**Manage Processes** opens the screen headed *"Workflow for {your title}"*,
with its own reminder:

> *"Workflow commands run automatically every turn. Add preconditions to
> control when it fires; an unmet precondition still shows its message
> every turn, it does not silently skip."*

Use Processes for anything that should tick along in the background — a
recurring hazard, a running score or turn check, a condition that ends the
game the moment it becomes true. The **Verb** is *not* required here (a
Process doesn't need to match typed input — it just runs).

That second sentence of the reminder is the single most common surprise:

> **Note:** A Process whose preconditions **fail** is not silent. If one of
> its preconditions is set up to show a message on failure (or you've built
> it to always print something), that message appears **every single turn**
> the precondition doesn't hold — not just once. Keep Processes narrow, and
> check what happens on the *failing* path, not just the passing one. If you
> want "only say something when a specific command is typed," you want a
> **Response**, not a Process.

## Responses: intercept a typed command

**Manage Responses** opens the screen headed *"Responses for {your title}"*,
with its own reminder:

> *"A response fires only when the player's verb (and adjective/noun, if set)
> matches exactly, short-circuiting the normal location/item lookup. If its
> preconditions aren't met, it falls through silently to normal handling —
> no message is shown. Matching a built-in verb (help, inventory, quit,
> look/describe) overrides that built-in for this adventure."*

Use Responses for adventure-wide command overrides: a custom `help` text, a
`pray` verb that works anywhere, a `score` command. The **Verb** *is*
required. When the player types something that matches a Response's
verb (and adjective/noun, if you set them):

- If the Response's preconditions pass, it runs and that's the whole turn —
  the normal "look in this location / on this item" search never happens.
- If they don't pass, nothing is shown and the game falls through to normal
  handling as if the Response weren't there.

Because a Response short-circuits normal lookup, giving one the same verb as
a built-in (**help**, **inventory**, **quit**, **look**/**describe**)
**replaces** that built-in for your adventure.

## What's next

Both Processes and Responses often want to print text — [Chapter 10:
Messages](10-messages.md) covers building a reusable library of it, and
editing the engine's own built-in messages.
