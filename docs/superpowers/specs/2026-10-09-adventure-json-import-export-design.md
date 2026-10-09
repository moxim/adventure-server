# Adventure JSON Import / Export — Design

## Goal

An author can export an adventure as a single `.json` file and import that file into another
installation (a different MongoDB + MySQL), producing a working, independent copy owned by the importing
author. Success: export, then import into an empty DB, yields an adventure equivalent to the original that
can be edited and run.

## Decisions (agreed)

- **Import always assigns new ids** (like Duplicate). An import can never overwrite existing data;
  importing the same file twice creates two adventures.
- **Raw-document approach**, not Jackson over `*Data`: export and import work on the raw MongoDB documents,
  like `AdventureDuplicator`. No per-type JSON handling; the format follows the model automatically.
- **Out of scope:** MySQL data (users, author/player assignments), saved games. The importing user
  becomes the AUTHOR of the imported adventure.

## File format

UTF-8 JSON, one envelope:

```json
{
  "format": "adventurebuilder-adventure",
  "formatVersion": 1,
  "builderVersion": "<BuilderVersion.current() or null>",
  "exportedAt": "<ISO-8601 instant>",
  "documents": {
    "adventures": [ { ... } ],
    "locations":  [ ... ],
    "items":      [ ... ],
    "pictures":   [ ... ],
    "...": []
  }
}
```

- Each document is canonical Extended JSON (`$date`, `$binary`, `{"$ref":..,"$id":..}`), so dates,
  picture `byte[]` content (base64) and DBRefs keep their types.
- `documents.adventures` holds exactly one document: the exported adventure.
- Suggested download filename: `<slugified title>.adventure.json`.

## Backend (`com.pdg.adventure.server.storage.service`)

1. **Shared helper.** Extract from `AdventureDuplicator` the graph logic — collect reachable documents via
   DBRefs, rewrite ids (strings and map keys) with an old→new map, insert with manual rollback — into one
   package-private/collaborator class. `AdventureDuplicator`, the exporter and the importer use it.
   Duplicate behaviour must not change (existing tests stay green).
2. **`AdventureExporter.export(String adventureId)`** → JSON bytes. Throws `IllegalArgumentException` if the
   adventure is missing.
3. **`AdventureImporter.importAdventure(byte[] json)`** → id of the new adventure. Validates (below), assigns
   new ULIDs, rewires references, stamps `createdAt`/`updatedAt`, inserts all-or-nothing (rollback on failure).

### Import validation (the file is untrusted)

Nothing is inserted unless the whole file passes. Failures raise a dedicated
`AdventureImportException` with a user-presentable message.

- Not JSON, or `format` ≠ `adventurebuilder-adventure` → "not an adventure file".
- `formatVersion` newer than this build's → "unsupported version".
- Collection names must be on an allowlist: `adventures`, `locations`, `containers`, `items`, `pictures`,
  `vocabularies`, `words` (every `@Document` collection except `savedgames`). Anything else is rejected, so a
  crafted file cannot write elsewhere. Kept as one constant, with a test that fails when a new `@Document`
  collection is added without a decision on it.
- Exactly one document in `adventures`.
- Every DBRef must resolve to a document in the file; a dangling reference rejects the import.
- Size limit on the upload (configurable, default 50 MB; pictures are base64).
- `builderVersion` differing from the running build is **not** an error; the caller gets a warning flag.

## Access (`AdventureAccessService`)

- `exportAdventure(String adventureId, UserData user)` — requires `canWrite` (ADMIN or owning AUTHOR), same
  rule as duplicate. `// TODO: Review needed — players cannot export; alternative: allow anyone who can read`.
- `importAdventure(byte[] json, UserData user)` — any AUTHOR or higher. `@Transactional`; imports into Mongo
  first, then saves the `AdventureAuthor` row; if the MySQL write fails the Mongo documents are removed
  (same compensation as `duplicateAdventure`). Returns the new `AdventureData` plus the version-mismatch flag.
- `// TODO: Review needed — imported title is kept as-is; alternative: suffix when the author already has that title`.

## UI (`AdventuresMenuView`)

- Grid context-menu item **Export** → browser download of the file.
- Button **Import Adventure** → dialog with an `Upload` (`.json`, single file, max size from config).
  Success: new adventure added to the grid + success notification (plus a warning if builder versions differ).
  Failure: error notification with the exception's message; the grid is unchanged.
- Vaadin download/upload API to be confirmed against the Vaadin MCP / docs for the project's Vaadin version
  at implementation time.

## Testing

- Service round trip on embedded Mongo: export → delete original → import; compare the graph, including a
  picture's bytes and a variable's value (variable names cannot contain `.`, see `VariableData`).
- Importing into the same DB while the original exists produces an independent copy (no shared ids).
- Rejection cases: bad format, newer version, unknown collection, dangling DBRef, two adventures, not JSON.
- Failed insert leaves no partial documents.
- Access: player cannot export; imported adventure's author is the importing user; MySQL failure removes the
  Mongo documents.
- Browserless view test for the Import button / dialog and export menu item (context-menu clicks are driven
  via package-private methods, as for Duplicate/Delete).
- Fixtures via `TestSupporter`. Update `CHANGELOG.md` per `AGENTS-update-changelog.md`.
