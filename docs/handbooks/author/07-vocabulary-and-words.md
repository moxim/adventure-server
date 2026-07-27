# 7. Vocabulary & Words

Adventure Builder's parser only understands words you've explicitly taught
it. **Manage Vocabulary** on the Adventure Editor is where you build that
dictionary.

## The vocabulary list

A grid of every word in your adventure, showing the word text, its
**Type**, and its **Synonym** (if any). Three buttons:

- **Create Word**
- **Edit Word**
- **Edit Special Words** — see [below](#special-words).

## Adding or editing a word

Both **Create Word** and **Edit Word** open the same dialog:

| Field | Notes |
|-------|-------|
| **Word** | The text itself. |
| **Type** | A radio choice: Noun, Adjective, or Verb (and related engine types). |
| **Synonyms** | Optionally point this word at another as its canonical form. The dialog's own hint: *"A synonym has precedence over a type."* — pick a synonym and the word inherits that word's type. |

### The synonym cascade

If you change what a word is a synonym of, and other words already point at
the *old* synonym, Adventure Builder notices and asks you what to do:

- **Update All** — reroute every one of those other words to the new
  synonym too.
- **Skip** — leave them pointing at the old one.

If reassigning would also change those words' `Type` (since type follows
the synonym chain), you'll get an extra warning before it happens. This is
worth reading carefully — a few words changing type at once can quietly
break commands elsewhere that expected a specific type.

## Deleting a word

Refused if any command, item, location, or exit still references it — you'll
see the list of what's using it so you know what to fix first.

## Special words

**Edit Special Words** is a separate screen — *"Configure special words
that are used throughout the adventure for common actions"* — for the
handful of vocabulary slots the engine treats specially. Today it exposes
four of them:

| Field on screen | Wires up |
|-----------------|-----------|
| **Taker** | The word players use to pick things up (`take`, `get`, whatever you choose). Required before "Can be picked up" items can auto-generate their `take`/`drop` commands — see [Chapter 6](06-items.md#checking-can-be-picked-up). |
| **Dropper** | The word for putting something down. |
| **Loader** | The word for loading a saved game. |
| **Examiner** | The word used for examining things in detail. |

Each one can be changed freely — *unless* it's currently the verb on a
command belonging to one of your items (most commonly because it's doing
double duty as an auto-generated take/drop verb). Try to change or clear a
special word that's in use, and Adventure Builder blocks it and shows you
exactly which items are relying on it, so you can update them first.

> **Note:** Adventure Builder's underlying data model has more special-word
> slots than this screen exposes — inventory, look, go, help, quit, and save
> words also exist internally, but there's currently no screen to set them.
> In practice this rarely blocks you: when you **Test** or **Run** an
> adventure, `look`, `inventory`, `help`, and `quit` (plus a few synonyms
> like `l`, `x`, `i`, `exit`, `bye`) always work automatically, independent
> of vocabulary setup — see [Chapter 11](11-testing-and-running.md).

## What's next

With words in place, [Chapter 8](08-commands-actions-and-conditions.md)
covers turning them into actual gameplay: commands, actions, and
conditions.
