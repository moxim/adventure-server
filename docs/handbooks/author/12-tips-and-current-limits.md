# 12. Tips, gotchas & current limits

A round-up of things worth knowing that don't fit neatly into a single
chapter — some are safety nets working in your favor, some are rough edges
worth planning around.

## Things that protect you

- **Deletes that would break something are refused, not silently allowed.**
  Deleting a location, item, message, or vocabulary word that's still
  referenced elsewhere is blocked, and you're shown exactly what's using
  it — see [Chapter 5](05-locations-and-exits.md#the-locations-list),
  [Chapter 7](07-vocabulary-and-words.md#deleting-a-word),
  [Chapter 10](10-messages.md).
- **Most editors warn you before you navigate away with unsaved changes.**
  Everywhere the standard [Cancel/Reset/Back/Save bar](05-locations-and-exits.md#the-standard-editor-button-bar)
  applies, **Back** prompts for confirmation if you'd lose edits.

## Things to plan around

- **Two deletion paths skip confirmation entirely:** deleting an
  **adventure** from Your Adventures, and deleting an **exit** from a
  location's Exits list. Both remove the thing (and, for adventures,
  everything inside it) immediately on right-click → **Delete**, with no
  dialog and no undo. See
  [Chapter 3](03-your-adventures.md#deleting-an-adventure) and
  [Chapter 5](05-locations-and-exits.md#exits). Everything else described
  in this handbook (locations, items, words, messages, workflow commands)
  confirms before deleting.
- **The World map isn't wired to your actual locations yet.** It's a
  placeholder illustration, not a live diagram of your world — see
  [Chapter 5](05-locations-and-exits.md#the-world-map).
- **Special Words only exposes 4 of the model's 10 slots** (Taker, Dropper,
  Loader, Examiner). The others exist but have no editor screen today — in
  practice this rarely matters because the play screen's core verbs
  (`look`, `inventory`, `help`, `quit`) work automatically regardless. See
  [Chapter 7](07-vocabulary-and-words.md#special-words).
- **Workflow *Processes* print their failure message every turn, not once
  — and run once per command, not once per line.** If a Process's
  precondition is unmet, and that precondition (or the command) is set up to
  say something about it, expect to see that message every turn until the
  condition changes. Also note a Process runs once for *each* command in a
  turn, so `take key and open door` runs every Process twice. If you want
  "only speak up when a specific command is typed," build a Response, not a
  Process. See [Chapter 9](09-workflow.md).
- **A Workflow *Response* is a fallback, not an override of your own
  commands.** A Response fires only when the typed verb matches it exactly
  *and* nothing in the current location or the player's pocket handles that
  verb. If you give a location or an item a command with the same
  verb/adjective/noun as one of your Responses, the local command wins and
  the Response won't fire there. (Built-ins like `look` / `help` still get
  replaced by a same-verb Response, because nothing local defines them.)
  See [Chapter 9](09-workflow.md).
- **No save/load mid-playtest.** A Test or Run session is one continuous
  sitting; there's nothing to resume later. See
  [Chapter 11](11-testing-and-running.md#what-you-can-type).
- **Items always live where you created them.** There's no parent-container
  picker in the item editor — to move something programmatically, use a
  **Move Item** action. See [Chapter 6](06-items.md).
- **Conditions only combine with AND**, and there's no separate "Not"
  condition type — every condition row has a **Negate** checkbox instead.
  If you need "either/or" logic, build it as separate command-chain
  variants. See [Chapter 8](08-commands-actions-and-conditions.md#preconditions).

## Ownership, in short

- You can only author adventures an **Admin** has assigned to you. There's
  no self-service way to claim one.
- There's no self-service account creation either — an Admin creates your
  login.

## If something still doesn't make sense

This handbook is written against the app's actual current behavior, not
its aspirational design — if a screen genuinely doesn't match what's
described here, the code has likely moved since this was last verified.
Check with whoever maintains your Adventure Builder instance.
