# 9. Workflow: Processes & Responses

Everything in [Chapter 8](08-commands-actions-and-conditions.md) is scoped
to one location or item. **Workflow** commands are the opposite: they apply
no matter where the player is. There are two kinds, and they behave
differently:

| Kind | When it runs | If its preconditions aren't met |
|------|--------------|--------------------------------|
| **Process** | Automatically, before **every command** the engine handles — and each part of an `and` / `then` sentence counts as its own command | Still runs — and any message it's set up to show appears **every time** |
| **Response** | Only when the player types a command that **matches it exactly** *and* nothing in the current location or the player's pocket already handles that verb | The turn just fails for that verb (*"I can't do that"*, unless a precondition supplies its own message) |

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

> **One turn, several runs.** Processes run once *per command*, not once per
> line typed. If the player types `take key and open door`, that's two
> commands, so every Process runs twice that turn. Keep that in mind for a
> Process that counts turns or changes a score.

That second sentence of the reminder is the single most common surprise:

> **Note:** A Process whose preconditions **fail** is not silent. If one of
> its preconditions is set up to show a message on failure (or you've built
> it to always print something), that message appears **every single turn**
> the precondition doesn't hold — not just once. Keep Processes narrow, and
> check what happens on the *failing* path, not just the passing one. If you
> want "only say something when a specific command is typed," you want a
> **Response**, not a Process.

## Responses: a fallback for a typed command

**Manage Responses** opens the screen headed *"Responses for {your title}"*,
with its own reminder:

> *"A response is a fallback: it fires only when the player's verb (and
> adjective/noun, if set) matches exactly and nothing in the current
> location or the player's pocket already handles that verb. The verb is
> required. Matching a built-in verb (help, inventory, quit, look/describe)
> still overrides that built-in for this adventure."*

Use Responses for adventure-wide commands that nothing else defines: a
custom `help` text, a `pray` verb that works anywhere, a `score` command —
or, with the wildcard noun `~`, take/drop/wear/remove for every item at once
(see [below](#one-response-for-every-item-the-wildcard-noun)).
The **Verb** *is* required. When the player types something that matches a
Response's verb (and adjective/noun, if you set them):

- The engine first looks for a command on the current location or on an item
  in the room / the player's pocket. If it finds one, that's the turn — the
  Response is **not** consulted.
- Only if nothing local matches that verb does the Response get its turn. If
  its preconditions pass, it runs. If they don't, the turn fails for that
  verb (*"I can't do that"*, or whatever message a failing precondition
  carries).

So a Response only "wins" a verb that no location or item command claims.
Built-in **help** / **inventory** / **quit** / **look**/**describe** have no
location or item commands behind them, so a Response with one of those verbs
still **replaces** the built-in for your adventure. But if *you* give a
location or an item a command with the same verb/adjective/noun as one of
your Responses, that local command now wins and the Response won't fire
there.

## One Response for every item: the wildcard noun

Set a Response's **Noun** to `~` and it answers for **any** noun the player
types — including none, and including words the game doesn't know. It's the
same idea as the `_` in the original Professional Adventure Writer: *"`GET _`
matches whatever the player wants to get."* A Response for an exact noun
(say `take lamp`) always beats the `~` one.

Pair it with the automatic item actions, which find the item the player named
by themselves (carried items first, then the ones in the room):

| Response (verb + noun) | Action | What the player sees |
|------------------------|--------|----------------------|
| `take` + `~` | **AUTOT** | *"I now have the …"*; *"I already have the …"*; *"There isn't one of those here."*; *"I can't carry any more things."*; *"I can't do that."* if the word isn't an item |
| `drop` + `~` | **AUTOD** | *"I've dropped the …"*; *"I can't. I am wearing the …"* (it won't drop what you're wearing); *"I don't have the …"* (it's here, not carried); *"I don't have one of those."* |
| `wear` + `~` | **AUTOW** | *"I am now wearing the …"*; *"I'm already wearing the …"*; *"I can't wear the …"*; *"I don't have the …"*; *"I don't have one of those."* |
| `remove` + `~` | **AUTOR** | *"I've removed the …"*; *"I'm not wearing the …"*; *"I am not wearing any of those."* |

Add each as its own Response (Verb, Noun `~`, and a single AUTO action — they
take no settings). The first time you open a Response for editing, the `~` noun
is added to your vocabulary for you. One caveat: because items' own commands
are tried first, an item that still has its own `take` or `drop` command
answers itself and the wildcard Response never sees it.

## What's next

Both Processes and Responses often want to print text — [Chapter 10:
Messages](10-messages.md) covers building a reusable library of it, and
editing the engine's own built-in messages.
