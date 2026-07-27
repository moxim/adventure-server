# 9. Workflow: commands that run everywhere

Everything in [Chapter 8](08-commands-actions-and-conditions.md) is scoped
to one location or item. **Workflow** commands are the opposite: they run
automatically **every turn, no matter where the player is**, before
location-scoped commands get a chance to fire.

Use this for anything global — a recurring hazard, a running score check, a
`help` variant that should work anywhere, or a condition that should end
the game the moment it becomes true.

**Manage Workflow** on the Adventure Editor takes you here.

## Reading the screen

The screen opens with its own reminder, worth reading verbatim:

> *"Workflow commands run automatically every turn. Add preconditions to
> control when it fires; an unmet precondition still shows its message
> every turn, it does not silently skip."*

That second sentence is the important one, and it's the single most common
surprise about Workflow commands:

> **Note:** A workflow command whose preconditions **fail** is not silent.
> If one of its preconditions is set up to show a message on failure (or if
> you've built it to always print something), that message will appear
> **every single turn** the precondition doesn't hold — not just once. Keep
> workflow commands narrow, and double-check what happens on the failing
> path, not just the passing one.

## The grid and the editor

A grid lists every workflow command: **Verb**, **Adjective**, **Noun**,
**Preconditions**, **Actions** — same shape as a location's command list.
Three buttons sit above it:

- **New Command** — clears the editor for a fresh command.
- **Delete Command** — disabled until you select an existing row; asks for
  confirmation.
- **Back** — returns to the Adventure Editor.

Select a row to load it into the editor below: **Verb** / **Adjective** /
**Noun** pickers (same vocabulary-only pickers as everywhere else), then the
exact same **Preconditions & Actions** editor from
[Chapter 8](08-commands-actions-and-conditions.md) — same Add/Up/Down/Remove
controls, same Negate checkbox, same rules. **Save Command** commits the
currently-loaded command and resets the editor to a blank new one, ready
for the next.

There's no Command Chain concept here — workflow commands don't share a
verb/adjective/noun with variants the way location commands can; each one
stands alone.

## What's next

Both location commands and workflow commands often want to print text —
[Chapter 10: Messages](10-messages.md) covers building a reusable library of
it.
