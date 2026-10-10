# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Fixed
- Picking up, dropping, wearing or removing one of two items that share a noun no longer acts on the wrong one: with the blue swim suit in the pocket, `take neoprene suit` now takes the neoprene suit instead of answering "I already have the blue suit" (AutoTake, AutoDrop, AutoWear and AutoRemove check what is carried by the exact adjective typed).

### Added
- Export an adventure as a JSON file and import it into another database: authors can move an adventure between installations. Import always creates a new, independent adventure and validates the file first.
