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
| **Add Variable** | Adds the value of the first variable to the second one (*second = second + first*); the first stays as it is | The two variables, chosen from dropdowns |
| **AutoDrop** | Player drops the item named by the noun they typed; refuses if it's worn | Nothing — use it in a Response with the noun `~` |
| **AutoRemove** | Player takes off the worn item named by the noun they typed | Nothing — use it in a Response with the noun `~` |
| **AutoTake** | Player picks up the item named by the noun they typed | Nothing — use it in a Response with the noun `~` |
| **AutoWear** | Player puts on the carried item named by the noun they typed | Nothing — use it in a Response with the noun `~` |
| **Break** | Stops the rest of this action list — and the rest of the command chain — immediately | Nothing — pair it with a **Message** action if you want it to say something as it stops |
| **Create Item** | Places an item into a location (bringing it into play) | The item; the destination location |
| **Decrement Variable** | Subtracts an amount from a named variable | Variable name; amount |
| **Describe** | Prints the description of one fixed item or location | The target |
| **Examine** | Prints the long description of the item the player named, carried or here; says "There isn't one of those here." otherwise | Nothing — use it in a Response with your look verb and the noun `~` |
| **Destroy** | Deletes an item from the game permanently | The item |
| **Drop** | Player drops an item at the current location | The item |
| **Increment Variable** | Adds an amount to a named variable | Variable name; amount |
| **Inventory** | Lists everything the player carries | Nothing — fully automatic |
| **Light** | Sets an item's light level (lumen), e.g. turning a lamp on or off | The item; a lumen value from 0 (dark) to 100 (full brightness) |
| **Load Game** | Without a slot number lists the player's saved games; with one (`load 3`) restores that game and describes the place again | Nothing — use it in a Response with the verb `load` and the noun `~` |
| **Look** | Describes the location the player is in, in full, shows its picture and fires your Arrival Processes | Nothing — use it in a Response with your look verb, not in an Arrival Process (Look already fires those) |
| **Message** | Prints a message from your [message catalog](10-messages.md) | The message (picked by its summary) |
| **Msg** | Prints a message from your [message catalog](10-messages.md) like **Message**, but does **not** end the line: the text of the next action continues right after it (see below) | The message (picked by its summary) |
| **Move Item** | Moves an item into a different container | The item; the destination (a location or the player's pocket) |
| **Move Player** | Moves the player to a location and shows its description | Destination location |
| **Picture** | Shows a [picture](11-pictures.md) in the play screen's picture panel until the next move, look or Picture action | The picture |
| **Print** | Prints the value of a variable (just the number, e.g. *14*) and does **not** end the line, so the next text continues right after it (see below) | The variable, chosen from the dropdown |
| **Diagnose Variable** | Prints a variable's name and its current value, e.g. *"VISITED : 14"* | The variable, chosen from the dropdown (`VISITED` and `TURNS` are always listed) |
| **Diagnose Variables** | Prints every variable and its current value, one per line, sorted by name: *"TURNS : 3"*, *"VISITED : 14"* | Nothing - fully automatic |
| **Quit** | Ends the game session | Nothing — pair it with a **Message** action for a farewell line |
| **Random** | Sets a variable to a random whole number from 0 to 99 (both included) | The variable: pick an existing one or type a new name, which creates it (no error for a new name) |
| **Remove** | Takes a worn item off the player | The item |
| **Save Game** | Saves the running game into the slot the player typed (`save 3`), or the first free one; each player has 10 slots per adventure | Nothing — use it in a Response with the verb `save` and the noun `~` |
| **Set Variable** | Sets a named variable to an exact value | Variable name; value |
| **Subtract Variable** | Subtracts the value of the first variable from the second one (*second = second - first*); the first stays as it is | The two variables, chosen from dropdowns |
| **Take** | Player picks up an item into their pocket | The item |
| **Turns** | Tells the player how many turns they have taken: *"You have taken 1 turn."*, *"You have taken 5 turns."* | Nothing — it reads the standard `TURNS` variable (see below); its wording is system messages SM17 to SM20 |
| **Wear** | Player puts on a wearable item | The item |

**Joining texts on one line.** Every text-producing action in a command normally
starts on a new line. **Msg** and **Print** are the exception: they do not end
their line, so the text of the next action follows directly. This lets you build
a sentence from several parts, for example Msg *"You have "*, Print *score*,
Message *" points."* gives *"You have 14 points."*. A **Message** ends the line
after its own text. So if a Msg or Print is followed by an action with a text of
its own, such as **Turns**, the two run together without a line break: finish the
sequence with a **Message** (or put the break into the text). Joining works
inside one command only, not across commands, Processes or turns.

**Add Variable**, **Subtract Variable** and **Print** need variables that already exist;
naming an unknown one shows an error while you test the adventure. The result of an addition
or subtraction is a plain number and is not limited to a range.

Variable names are chosen from a dropdown, not typed. **Set Variable** and
**Random** are the actions that create a variable: pick an existing name or type
a new one. As soon as you enter the name in one of them, it shows up in the dropdowns of
**Increment Variable**, **Decrement Variable**, **Add Variable**, **Subtract Variable**, **Print**, the **Diagnose** actions and the variable conditions
(**Equals**, **Greater Than**, **Less Than**, **Same**), which accept only
names that are already set somewhere. The engine's own `VISITED` and `TURNS`
variables are always listed. The variable is stored with the adventure when you save the
command, and it stays defined even if you later delete the Set or Random action. A
condition naming a variable that was never defined is flagged as invalid when
you open it.

Variables are your own free-form named counters — use them for anything
that needs to be remembered across turns: a score, a flag ("door_unlocked"),
a counter of attempts, and so on. Nothing pre-declares them; a Set Variable
or Random action creates one.

For a dice roll, use **Random** on a variable such as `dice` and then test it with
**Greater Than** or **Less Than**: *"if dice is greater than 70, the lock opens"*.
Each time the action runs the variable gets a new value from 0 to 99, so the
roll stays the same only until the next Random on that variable.

### The standard variable `TURNS`

`TURNS` is a variable the engine keeps for you, the same idea as the turn
counter of the original Professional Adventure Writer. It starts at 0 whenever
a game starts, and goes up by one for every command the game understands,
whether or not that command then succeeds. Input the game could not make
sense of ("I was not able to understand any of that") is not a turn, and
`take key and open door` is two turns. The command that is being run already
counts, so a **Turns** action in a `turns` Response reports *"You have taken
1 turn."* the first time the player types it.

Use it like any other variable: test it with **Greater Than**, **Less Than**
or **Equals** (to end a timed adventure after 50 turns, say), or change it
with **Set Variable**, **Increment Variable** and **Decrement Variable**.
Loading a saved game restores the turn count it was saved with.

The **Turns** action prints the value using four
[system messages](10-messages.md), so you can reword them under **Manage
System Messages**: SM17 (*"You have taken "*, before the number), SM18
(*" turn"*), SM19 (*"s"*, added unless the number is exactly 1) and SM20
(*"."*). The spaces in SM17 and SM18 matter: keep them if you change the
wording.

## PreConditions

Preconditions gate a command or command variant — all of them must pass
(they're combined with AND) for the command to fire. Any condition can be
flipped with the **Negate** checkbox on its row.

| Condition | What it checks | What you configure |
|-----------|------------------|----------------------|
| **Adjective 2** | The player typed a specific *second* adjective | The adjective, picked from your vocabulary |
| **Adverb** | The player typed a specific adverb | The adverb, picked from your vocabulary |
| **Carried** | The item is in the player's inventory | The item |
| **Chance** | A random roll succeeds | A chance percentage (1–100) |
| **Equals** | A variable equals a value | Variable name; value |
| **Greater Than** | A variable is greater than a value | Variable name; value |
| **Here** | The item is at the player's current location | The item |
| **Item At** | An item is at a given location | The item; the location |
| **Lower Than** | A variable is less than a value | Variable name; value |
| **Noun 2** | The player typed a specific *second* noun | The noun, picked from your vocabulary |
| **Player At** | The player is at a given location | The location |
| **Preposition** | The player typed a specific preposition | The preposition, picked from your vocabulary |
| **Same** | Two variables hold equal values | Two variable names |
| **Worn** | The item is currently worn | The item |

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
