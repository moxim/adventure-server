# Appendix A: Action & Condition reference

Every Action and PreCondition type available in the
[command](08-commands-actions-and-conditions.md) and
[workflow](09-workflow.md) editors, in one place. Add these via the
**Select Action Type** / **Add Condition** dropdowns.

## Actions

Actions are combined into an ordered list on a command and all run, in
order, when that command fires.

| Action | What it does | What you configure |
|--------|----------------|----------------------|
| **Message** | Prints a message from your [message catalog](10-messages.md) | The message id |
| **Move Player** | Moves the player to a location and shows its description | Destination location |
| **Move Item** | Moves an item into a different container | The item; the destination (a location or the player's pocket) |
| **Take** | Player picks up an item into their pocket | The item |
| **Drop** | Player drops an item at the current location | The item |
| **Wear** | Player puts on a wearable item | The item |
| **Remove** | Takes a worn item off the player | The item |
| **Destroy** | Deletes an item from the game permanently | The item |
| **Create Item** | Places an item into a location (bringing it into play) | The item; the destination location |
| **Describe** | Prints the description of an item or location | The target |
| **Inventory** | Lists everything the player carries | Nothing — fully automatic |
| **Quit** | Ends the game session | Nothing — pair it with a **Message** action for a farewell line |
| **Break** | Stops the rest of this action list — and the rest of the command chain — immediately | Nothing — pair it with a **Message** action if you want it to say something as it stops |
| **Increment Variable** | Adds an amount to a named variable | Variable name; amount |
| **Decrement Variable** | Subtracts an amount from a named variable | Variable name; amount |
| **Set Variable** | Sets a named variable to an exact value | Variable name; value |
| **Light** | Sets an item's light level (lumen), e.g. turning a lamp on or off | The item; a lumen value from 0 (dark) to 100 (full brightness) |

Variables are your own free-form named counters — use them for anything
that needs to be remembered across turns: a score, a flag ("door_unlocked"),
a counter of attempts, and so on. Nothing pre-declares them; the first
action that sets or changes one effectively creates it.

## PreConditions

Preconditions gate a command or command variant — all of them must pass
(they're combined with AND) for the command to fire. Any condition can be
flipped with the **Negate** checkbox on its row.

| Condition | What it checks | What you configure |
|-----------|------------------|----------------------|
| **Carried** | The item is in the player's inventory | The item |
| **Here** | The item is at the player's current location | The item |
| **Worn** | The item is currently worn | The item |
| **Player At** | The player is at a given location | The location |
| **Item At** | An item is at a given location | The item; the location |
| **Equals** | A variable equals a value | Variable name; value |
| **Greater Than** | A variable is greater than a value | Variable name; value |
| **Lower Than** | A variable is less than a value | Variable name; value |
| **Same** | Two variables hold equal values | Two variable names |
| **Chance** | A random roll succeeds | A chance percentage (1–100) |
| **Preposition** | The player typed a specific preposition | The preposition, picked from your vocabulary |
| **Adverb** | The player typed a specific adverb | The adverb, picked from your vocabulary |
| **Noun 2** | The player typed a specific *second* noun | The noun, picked from your vocabulary |
| **Adjective 2** | The player typed a specific *second* adjective | The adjective, picked from your vocabulary |

**Chance** is worth calling out for design: it re-rolls every time the
command is attempted, so a 20% chance condition doesn't mean "one in five
players" — it means "roughly one in five *attempts*," which matters if a
player can just try again.

**Preposition / Adverb / Noun 2 / Adjective 2** check parts of the typed
command beyond the command editor's own Verb/Adjective/Noun pickers — they
let one command variant tell "switch lamp on" apart from "switch lamp off",
or "use spanner on ancient machine" apart from "use spanner on rusty
engine", without needing a separate word in the vocabulary for every
combination. A mismatch fails silently (no message of its own), so pair
these with the ordinary Command Chain pattern: a variant with the matching
Preposition/Adverb/Noun 2/Adjective 2 condition higher in the chain, a
catch-all variant beneath it.

## See also

- [Chapter 8: Commands, Actions & Conditions](08-commands-actions-and-conditions.md)
  for how these combine into a command, and what the Command Chain does.
- [Chapter 9: Workflow](09-workflow.md) for using these outside a specific
  location.
