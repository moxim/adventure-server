# 11a. Sounds

A **Sound** is an mp3 file you upload to your adventure and play at a chosen moment with a **Play** action — a door
creaking, a ship's horn, a tune in the tavern. Sounds are heard in the running game; they never appear in the text.

## The sounds list

**Sounds** on the [Adventure Editor](04-the-adventure-editor.md) opens a list of your adventure's sounds — a running
**Sounds: N** count, plus **Edit Sound** (enabled once you select a row), **Create Sound** and **Back** on the left; a
**Find sound** search field (filters by name) and the grid on the right. The grid shows a **Play** button for each
sound, its **Name**, its **Size** and **Used** — how many Play actions use it. Click a sound's play button to hear it;
click it again to stop it. With no sounds yet the grid reads *"No sounds found. Upload some to bring your adventure to
life."*

Double-click a row to edit it, or right-click for **Edit**, **Find Usage** and **Delete**.

**Delete** is refused while a Play action still uses the sound: instead of the confirmation dialog you're shown the
list of places that use it, so you can clear them first. It counts wherever a command can hold actions — a location's
commands or exits, an item's commands (lying in a location or carried in the pocket) and the three
[workflow](09-workflow.md) lists, Arrival Processes included. An unused sound asks for confirmation and is then removed
for good.

## The sound editor

| Field | Notes |
|-------|-------|
| **Name** | Required, and **unique within the adventure** (upper and lower case and spaces around the name don't count: *"Tavern"* and *" tavern"* are the same name). It's what you pick in the **Play** action. |
| **Upload** | One file: **mp3**, **at most 6 MB**. |

Adventure Builder looks at the file's actual contents, not its name. A file that merely *ends* in `.mp3` but isn't
really an mp3 is refused with *"The uploaded file is not a valid mp3 file."*, and one that's too large with *"Sounds
must be mp3 files of at most 6MB"*. Once a file is uploaded, a play button lets you listen before you save.

The usual [Cancel / Reset / Back / Save bar](05-locations-and-exits.md#the-standard-editor-button-bar) applies. **Save**
stays disabled until the sound has a name **and** a file, and something has changed. A freshly uploaded file is only
*staged* until you press **Save**; **Reset** throws it away. To replace the file of an existing sound, open it and
upload a new one; to rename it, change the name and save — actions that use it keep working, because they refer to the
sound itself, not to its name. **Delete** removes a sound that no Play action uses.

## The Play action

A **Play** action takes a sound and a number from 0 to 99. Add it to any command's list of actions like any other
action (see [Chapter 8](08-commands-actions-and-conditions.md)).

| Times | What happens |
|-------|--------------|
| **1 to 99** | The sound starts at once and plays that many times in a row — `PLAY pirate_bay 2` plays *pirate_bay* twice. |
| **0** | Stops **every copy** of that sound that is playing. |

- A Play never interrupts anything: a second sound is heard together with the first, and playing a sound that is
  already playing starts another copy on top of it.
- **Sounds keep playing when the player moves to another location.** That's what `PLAY … 0` is for — add it to the
  commands or Arrival Processes of the place where the sound should end.
- When the game ends or the player quits, the sounds that are playing stop; a sound started by the very command that
  ends the game (a closing fanfare) is still heard, and ends when the player leaves the play screen. Leaving the play
  screen or restarting the adventure stops everything.
- A Play action tells the player nothing and never fails: a sound you deleted (or that can't be played) is just silent.
- Sounds are not part of a saved game; loading a game doesn't start them again.
- Sounds make an exported adventure file big (about a third more than the sound files themselves). An adventure with
  many sounds may be larger than the import limit of 50 MB; ask whoever runs the server to raise
  `adventure.import.max-bytes`.

> **Note:** Browsers don't allow a page to make sound before the player has done something on it. A Play that happens
> before the player's **first key press, click or touch** is skipped — so a Play in the opening room is normally
> silent. Put sounds you must have behind the player's first move.
