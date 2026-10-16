# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Fixed
- Picking up, dropping, wearing or removing one of several items that share a noun no longer acts on the wrong one: AutoTake looks at the room first, AutoDrop, AutoWear and AutoRemove at the pocket first, and a typed adjective always wins (`take neoprene suit` with the blue swim suit in the pocket now takes the neoprene suit instead of answering "I already have the blue suit"). When the first place still holds several candidates and no adjective was typed, the game asks "Which suit should I take?"; only fitting items count (remove considers worn items, wear unworn wearable ones, drop unworn ones, take containable ones). Take, drop, wear and remove commands built by hand on items follow the same rules (take prefers the room, the others the pocket; wear and remove only consider fitting items; "I already have the suit." / "I don't have the suit." / "I'm not wearing the suit." when the match is only in the other place).
- The texts of the Turns, Diagnose Variable, Diagnose Variables and Inventory actions now appear in the order of the command's actions; before, they showed up ahead of the texts of Message actions in the same command.

### Added
- A Turns action, as in the Professional Adventure Writer: authors can add it to a command or response to tell the player "You have taken 5 turns." The count is a new standard variable, TURNS, that every game starts at 0 and that goes up by one for each command the game understands; authors can also test or change it like any other variable. The wording is system messages SM17 to SM20, and the default of SM18 is now " turn" (with its leading space) so the sentence reads correctly.
- Add Variable and Subtract Variable actions, as ADD and MINUS in the Professional Adventure Writer: the value of the first variable is added to, or subtracted from, the second one. Naming a variable that does not exist shows an error while the author tests the adventure.
- A Random action (PAW's RANDOM): sets a variable to a random whole number from 0 to 99. A variable that does not exist yet is created instead of causing an error.
- A Msg action (PAW's MES) and a Print action (PAW's PRINT): like Message and Diagnose Variable they show a text, but without ending the line, so several can be strung together into one sentence, e.g. Msg, Print, Message gives "You have 14 points.". A Message ends the line after its text. Joining works within one command only.
- A Diagnose Variable action (DIAGNOSE_VAR): authors pick a variable and the game prints its name and current value, for example "VISITED : 14". Handy for scores, counters and for testing an adventure.
- A Diagnose Variables action (DIAGNOSE_VARS): the game lists every variable with its current value, one per line, sorted by name.
- Export an adventure as a JSON file and import it into another database: authors can move an adventure between installations. Import always creates a new, independent adventure and validates the file first.

### Removed
- The Create Item action (CREATE) is gone from the action list and the engine. Commands saved with it can no longer be loaded; replace it with another way of bringing the item into play.
