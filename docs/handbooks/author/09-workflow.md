# 9. Workflow: Processes, Arrival Processes & Responses

Everything in [Chapter 8](08-commands-actions-and-conditions.md) is scoped
to one location or item. **Workflow** commands are the opposite: they apply
no matter where the player is. There are three kinds, and they behave
differently:

| Kind | When it runs | If its preconditions aren't met |
|------|--------------|--------------------------------|
| **Process** | Automatically, before **every command** the engine handles — and each part of an `and` / `then` sentence counts as its own command | Still runs — and any message it's set up to show appears **every time** |
| **Arrival Process** | Automatically, right after a location's description is shown — when the player arrives there by moving, and again on every `look` | Does nothing that time — no error (unless a precondition carries its own failure message, which is then shown) |
| **Response** | Only when the player types a command that **matches it exactly** *and* nothing in the current location or the player's pocket already handles that verb | The turn just fails for that verb (*"I can't do that"*, unless a precondition supplies its own message) |

All three are reached from the [Adventure Editor](04-the-adventure-editor.md):
**Workflow I** (Arrival Processes), **Workflow II** (Processes) and
**Responses**. All three screens look and work
the same way — a grid of commands on top, a single-command editor below,
with **Back**, **New**, **Delete** (asks for confirmation), and **Save**
buttons. Selecting a grid row loads it into the editor; the editor itself is
the exact same **Verb / Adjective / Noun** pickers and **Preconditions &
Actions** editor from [Chapter 8](08-commands-actions-and-conditions.md) —
same Add/Up/Down/Remove controls, same **Negate** checkbox, same rules.
**Save** commits the loaded command and resets the editor to a blank new
one.

There's no Command Chain concept in any of these screens — a Workflow command
doesn't share a verb/adjective/noun with variants the way a location command
can. Each one stands alone.

## Processes: run every turn

**Workflow II** opens the screen headed *"Workflow for {your title}"*,
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

## Arrival Processes: run when a location is described

**Workflow I** opens the screen headed *"Arrival Processes for {your title}"*.
Its own reminder says that *"an arrival process runs automatically after a
location's description is shown"* — and that the **Verb / Adjective / Noun**
are *not* matched against what the player types, they only label the entry for
you. The **Verb** is not required here either.

Use Arrival Processes for anything that belongs to *being in a place* rather
than to a typed command: an ambient line ("Waves slap against the jetty."), an
event the first time someone enters, or a [picture](11-pictures.md) you want on
every visit.

- **When they run.** After the description of the location is printed, so the
  description comes first and the output of your Arrival Processes follows
  it. That happens when the player **arrives by moving** (including the
  automatic arrival when the play screen opens) and again on every explicit
  **`look`**.
- **They fire every time.** Looking around again fires them again; arriving a
  second time fires them again. If you want something to happen only once,
  guard it yourself — for example, an action that sets a variable the first
  time, plus a variable condition that checks it.
- **They apply to the whole adventure, not one location.** Add a **Player At**
  precondition to limit an entry to a particular location. A precondition that
  isn't met just means nothing happens that time; as with Processes, though, if
  the precondition is set up to show a message on failure, you'll see it.

> **Note:** Arrival by moving always fires them. The *explicit `look`* path can
> be bypassed in three cases: you've defined your own Response for the verb
> `describe`; a location or item command matches `describe` / `look`; or you've
> attached a **Describe** action to one of your commands. In those cases a
> `look` runs your version instead and the Arrival Processes aren't triggered by
> it.

## Responses: a fallback for a typed command

**Responses** opens the screen headed *"Responses for {your title}"*,
with its own reminder:

> *"A response is a fallback: it fires only when the player's verb (and
> adjective/noun, if set) matches exactly and nothing in the current
> location or the player's pocket already handles that verb. The verb is
> required."*

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
**help** / **inventory** / **quit** / **look**/**describe** are ordinary
Responses too (see
[Looking, inventory, help and quit](07-vocabulary-and-words.md#looking-inventory-help-and-quit)).
If *you* give a location or an item a command with the same
verb/adjective/noun as one of your Responses, that local command wins and
the Response won't fire there.

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
| `take` + `~` | **AutoTake** | *"I now have the …"*; *"I already have the …"*; *"There isn't one of those here."*; *"I can't carry any more things."*; *"I can't do that."* if the word isn't an item |
| `drop` + `~` | **AutoDrop** | *"I've dropped the …"*; *"I can't. I am wearing the …"* (it won't drop what you're wearing); *"I don't have the …"* (it's here, not carried); *"I don't have one of those."* |
| `wear` + `~` | **AutoWear** | *"I am now wearing the …"*; *"I'm already wearing the …"*; *"I can't wear the …"*; *"I don't have the …"*; *"I don't have one of those."* |
| `remove` + `~` | **AutoRemove** | *"I've removed the …"*; *"I'm not wearing the …"*; *"I am not wearing any of those."* |

If the player types an adjective, the automatic actions take it seriously when
asking *"is it carried or worn?"*: with a blue swim suit in the pocket,
`take neoprene suit` picks up the neoprene suit from the room, and `drop neoprene
suit` or `wear neoprene suit` say *"I don't have the neoprene suit."* instead of
quietly using the blue one. Without an adjective, any item with that noun will do
(`drop suit` drops the one you carry). When nothing in the pocket fits, the room is
searched by noun alone, so an adjective that matches nothing there is ignored.

Add each as its own Response (Verb, Noun `~`, and a single AUTO action — they
take no settings). The first time you open a Response for editing, the `~` noun
is added to your vocabulary for you. One caveat: because items' own commands
are tried first, an item that still has its own `take` or `drop` command
answers itself and the wildcard Response never sees it.

### Saving and loading

The same wildcard lets players keep their progress. Create the verbs `save` and `load` in your vocabulary, then add
two Responses, each with a single action that takes no settings:

| Response (verb + noun) | Action | What the player can type |
|------------------------|--------|--------------------------|
| `save` + `~` | **Save Game** | `save` (first free slot), `save 3` (slot 3, replacing what was there) |
| `load` + `~` | **Load Game** | `load` (lists the saved games), `load 3` (restores slot 3) — or `load`, then just `3` |

Every player has 10 slots per adventure; the list reads *"1. The Demo - 2026-10-07 13:43:45"*. A load puts the game
back in place — locations, items, what is worn, lights, variables — and describes where the player is. If you change the
adventure after a player saved, things that no longer exist are skipped and a save whose location is gone says it can no
longer be loaded. Items you added after the save stay where they are. Numbers above 10 are not words to the game, so
`save 11` behaves like a plain `save`.

Things to know:

- The slot numbers `1` to `10` are the only words the engine adds to the game's vocabulary itself. If you have already used one of those
  words yourself (as a verb or a synonym), your meaning wins and that slot number can't be typed.
- Each save remembers which version of the adventure builder last saved your adventure. When a player loads a game
  and the version has changed since, the game adds a short note saying so.
- Players can't delete a save, only overwrite it. A save is private to the player and the adventure.
- The wording of all these messages (*game saved*, *no free slot*, ...) can be changed in [Chapter 10:
  Messages](10-messages.md).

## What's next

Workflow commands often want to print text — [Chapter 10:
Messages](10-messages.md) covers building a reusable library of it, and
editing the engine's own built-in messages — or show an image;
[Chapter 11: Pictures](11-pictures.md) covers that.
