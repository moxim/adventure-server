# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Fixed
- Picking up, dropping, wearing or removing one of several items that share a noun no longer acts on the wrong one: AutoTake looks at the room first, AutoDrop, AutoWear and AutoRemove at the pocket first, and a typed adjective always wins (`take neoprene suit` with the blue swim suit in the pocket now takes the neoprene suit instead of answering "I already have the blue suit"). When the first place still holds several candidates and no adjective was typed, the game asks "Which suit should I take?"; only fitting items count (remove considers worn items, wear unworn wearable ones, drop unworn ones, take containable ones). Take, drop, wear and remove commands built by hand on items follow the same rules (take prefers the room, the others the pocket; wear and remove only consider fitting items; "I already have the suit." / "I don't have the suit." / "I'm not wearing the suit." when the match is only in the other place).

### Added
- Export an adventure as a JSON file and import it into another database: authors can move an adventure between installations. Import always creates a new, independent adventure and validates the file first.
