# 7. Vocabulary & Words

Adventure Builder's parser only understands words you've explicitly taught
it. **Vocabulary** on the Adventure Editor is where you build that
dictionary.

## The vocabulary list

A grid of every word in your adventure, showing the word text, its
**Type**, and its **Synonym** (if any). Two buttons:

- **Create Word**
- **Edit Word**

## Adding or editing a word

Both **Create Word** and **Edit Word** open the same dialog:

| Field | Notes |
|-------|-------|
| **Word** | The text itself. |
| **Type** | A radio choice between all seven word types: **Verb**, **Noun**, **Adjective**, **Adverb**, **Preposition**, **Conjunction**, and **Pronoun**. *Conjunction* and *Pronoun* are the two special ones: a Conjunction (`and`, `then`) lets the player chain commands in one line, and a Pronoun (`it`) stands for the last thing they mentioned. The engine adds no words of its own, so your vocabulary needs at least one of each if you want chaining and `it` to work. |
| **Synonyms** | Optionally point this word at another as its canonical form. The dialog's own hint: *"A synonym has precedence over a type."* — pick a synonym and the word inherits that word's type. |

### Adverbs and Prepositions

Most commands are built from a verb plus an optional adjective and noun (see
[Chapter 8](08-commands-actions-and-conditions.md)) — but the parser also
recognizes two more word types that let a single typed command carry extra
detail without needing a whole separate verb for every variant:

- **Adverb** — modifies *how* the action is done, and can appear before the
  verb: `SLOWLY OPEN CHEST`.
- **Preposition** — relates the noun to something else, and typically
  appears after it: `SWITCH LAMP ON`.

Neither is captured by the Verb/Adjective/Noun pickers in the command
editor — there's no built-in vocabulary for either, so if you want a
command to respond to a specific adverb or preposition, you have to create
that word yourself here first. Once it exists, attach an **Adverb** or
**Preposition** [condition](08-commands-actions-and-conditions.md) to a
command variant to check for it — e.g. a `Preposition: on` condition on one
"switch lamp" variant and `Preposition: off` on another, so the same
verb+noun pair does two different things depending on what the player
typed. A command that's built without an Adverb/Preposition condition
doesn't care whether one was typed at all — it's purely additive.

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

## Looking, inventory, help and quit

There is no screen for "special" words: taking, dropping and the other
verbs are ordinary Responses you write yourself. The engine adds no words
of its own: your vocabulary has to contain `describe` (with synonyms such as
`look`, `l`, `examine`, `x`), `inventory`, `help` and `quit` before the
parser can recognise them, and what they *do* is up to your
[Responses](09-workflow.md#responses) — the engine has no built-in
behaviour behind any of them. A basic set is:

| Verb | Noun | Actions |
|------|------|---------|
| `describe` | *(none)* | **Look** |
| `describe` | `here` | **Look** |
| `describe` | `~` | **Examine** |
| `inventory` | *(none)* | **Inventory** |
| `help` | *(none)* | **Message** with your help text |
| `quit` | *(none)* | **Message** with a farewell, then **Quit** |

**Look** describes the player's location in full (with its picture) and
fires your Arrival Processes. **Examine** shows the long description of the
item the player named, whether carried or in the room; with a noun that
names nothing here it says "There isn't one of those here." (message 26 on
the *System Messages* screen, which you can reword). Because Responses are
only a fallback, an item or location command for the same verb still wins.

## The wildcard noun `~`

The noun `~` is a special word that means *"any noun"*. It appears in your
vocabulary automatically the first time you open a Response for editing, and
you can pick it in a Response's **Noun** field. It can't be renamed, retyped
or made a synonym — Adventure Builder blocks that. See
[Chapter 9](09-workflow.md#one-response-for-every-item-the-wildcard-noun) for
how to use it.

## What's next

With words in place, [Chapter 8](08-commands-actions-and-conditions.md)
covers turning them into actual gameplay: commands, actions, and
conditions.
