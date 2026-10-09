# 03 — Domain Model

## Purpose

This chapter is the canonical inventory of the **domain entities**: every business
object (BO) and every persistence document (DO), how they relate, and how their
identity, lifecycle, and naming are organised. Implementation choices about *where*
the data lives and *how* the layers translate are split off into
[`05-persistence-and-mappers.md`](05-persistence-and-mappers.md). Runtime semantics
(what makes the domain *do* something) live in
[`04-runtime-engine.md`](04-runtime-engine.md).

## Layering: BO vs DO

The domain has two parallel object hierarchies:

- **Business Objects (BO)** — runtime, stateful, behaviour-rich. They live in
  `server.<domain>` packages. Examples: `Adventure`, `Location`, `Item`,
  `GenericContainer`, `Vocabulary`, `Word` (BO-side wrapper of the data class
  by the same name; identity is preserved across layers because `Word` is the
  same class on both sides — see "Vocabulary" below).
- **Data Objects (DO)** — persistence-shaped: MongoDB `@Document` documents,
  MySQL `@Entity` rows. They live in `model/` (Mongo) and `security/model/`
  (JPA). All names end in `Data` *except* the JPA security entities and `Word`,
  which is the only data class without the suffix because it doubles as both
  representation and runtime structure.

Translation between the two is the responsibility of the **mapper layer**
(see [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md)).
A few entities (notably `Word`) are shared verbatim across the two layers.

## Naming conventions

Class-name suffixes are normative — they are how the codebase identifies the
role of a type.

| Role | Suffix | Example |
|------|--------|---------|
| Business Object | *(none)* | `Location`, `Item`, `Adventure` |
| Persistence Document / JPA Entity | `*Data` | `LocationData`, `ItemData`, `UserData` |
| Spring Data Repository | `*Repository` | `LocationRepository`, `UserRepository` |
| Service | `*Service` | `AdventureService`, `UserService` |
| Mapper | `*Mapper` | `LocationMapper`, `ActionMapper` |
| Action (engine command pattern) | `*Action` | `TakeAction`, `MoveItemAction` |
| Condition | `*Condition` | `CarriedCondition`, `WornCondition` |
| Exception | `*Exception` | `QuitException`, `ItemNotFoundException` |
| Vaadin layout / view / editor | `*Layout`, `*View`, `*EditorView`, `*MenuView`, `*Editor` | see [`07-ui-and-navigation.md`](07-ui-and-navigation.md) |
| View model | `*ViewModel` | `LocationViewModel`, `ItemViewModel` |
| Adapter (description / picker bridges) | `*Adapter` | `LocationDescriptionAdapter` |

**Word is the documented exception.** It is a single class used in both layers.

## Identity strategy

| Layer | Generator | Field |
|-------|-----------|-------|
| MongoDB documents | ULID via `com.github.f4b6a3:ulid-creator` 5.2.4, lower-cased | `BasicData.id` (assigned in field initialiser) |
| MySQL entities (`UserData`, `AdventureAuthor.adventureId`, `AdventurePlayerId.{adventureId,userId}`) | ULID via `UlidCreator` (`@PrePersist` for `UserData`; explicitly set for the others) | `id`, all `length = 26`, `nullable = false`, `updatable = false` |

ULIDs are 26 lower-cased characters. The MySQL columns are sized accordingly so
that an `AdventureData.id` value can be used verbatim as
`AdventureAuthor.adventureId` to bind ownership across stores.

## Inheritance roots

```
BasicData            (Ided; has @Id String id assigned to ULID)
└── DatedData         (+@CreatedDate, @LastModifiedDate; touch())
    └── ThingData     (+DescriptionData, CommandProviderData)
        ├── LocationData
        └── ItemData      (+adventureId, locationId, isContainable, parentContainerId, isWearable, isWorn)
            └── ItemContainerData  (+items: List<ItemData>, maxSize, holdingDirections)
└── DatedData
    └── AdventureData
└── DatedData
    └── VocabularyData
└── DatedData
    └── MessageData       (summary, text)
└── BasicData
    └── PictureData       (+adventureId, name, content: byte[], contentType)
└── DatedData
    └── SystemMessageData (key, text — one editable override of a
                            SystemMessageKey catalog entry; see "System messages" below)
└── DatedData
    └── Word              (text, type, synonym)
└── BasicData
    └── DirectionData     (+descriptionData, destinationId, destinationMustBeMentioned, commandData)
└── BasicData
    └── CommandData       (+commandDescription, preConditions, actions: List<ActionData>)
└── BasicData
    └── CommandChainData  (+commands: List<CommandData>)
   (WorkflowData           +commands: List<CommandData> (Processes)
                           +interceptorCommands: List<CommandData> (Responses)
                           +arrivalProcesses: List<CommandData> (Arrival Processes)
                            — a plain embedded field on AdventureData, not part of
                            the BasicData hierarchy; see "Workflow" below)
└── BasicData
    └── CommandProviderData (+availableCommands: Map<String, CommandChainData>)
└── BasicDescriptionData
    └── DescriptionData       (+shortDescription, longDescription)
    └── CommandDescriptionData (+verb, [adjective], [noun])
```

`BasicDescriptionData` is the parent of both display descriptions and command
descriptions because both share noun/adjective slots.

## Aggregate: Adventure

### Adventure (BO)

`server/Adventure.java` is the runtime aggregate root.

Holds:

- `id`, `title`, `currentLocationId`, `notes`.
- `pocket: GenericContainer` — the player's personal container.
- `locationMap: Map<String, Location>` — keyed by location id.
- `allItems: Map<String, Item>`, `allContainers: Map<String, Container>` — cross-
  cutting indices populated during mapping for fast lookup by id.
- `vocabulary: Vocabulary`, `messagesHolder: MessagesHolder`, `variableProvider: VariableProvider`.

### AdventureData (DO)

`model/AdventureData.java`, collection `adventures`, extends `DatedData`.

Fields:

| Field | Type | Notes |
|-------|------|-------|
| `title` | `String` | Display name. |
| `playerPocket` | `@DBRef(lazy=true) ItemContainerData` | The player's pocket. Cascade save & delete. |
| `locationData` | `@DBRef(lazy=false) Map<String, LocationData>` | All locations, keyed by id. Cascade save & delete. |
| `currentLocationId` | `String` | Resolves into `locationData`. |
| `vocabularyData` | `@DBRef(lazy=false, transient) VocabularyData` | The adventure's vocabulary. Cascade save & delete. |
| `messages` | `Map<String, MessageData>` | Author-authored reusable text, keyed by each message's `id`. Embedded (owned 1:1 by the adventure) — no `@DBRef`, no cascade annotations. See [§ MessageData](#messagedata). |
| `systemMessages` | `Map<String, SystemMessageData>` | Sparse per-adventure overrides of engine text; keyed by `SystemMessageKey.id()`. Embedded — no `@DBRef`, no cascade annotations. A key with no entry reads as its catalog default. See [§ System messages](#system-messages) below. |
| `pictureData` | `@DBRef(lazy=false) Map<String, PictureData>` | The adventure's uploaded pictures, keyed by id. `@CascadeDelete` only — **no** `@CascadeSave`: `PictureEditorView` saves each picture explicitly (`AdventureService.savePictureData`) before saving the adventure. See [§ PictureData](#picturedata). |
| `font` | `AdventureFont`, default `DEFAULT` | The font of the run view's game text, stored as the constant's **name** (`DEFAULT`, `INTER`, `LORA`, `IBM_PLEX_MONO`, `MEDIEVAL_SHARP`, `CINZEL`, `OXANIUM`, `SHARE_TECH_MONO`, `SPECIAL_ELITE`, `IM_FELL_ENGLISH`, `COURIER_PRIME`, `ASIMOVIAN`, `AUDIOWIDE`, `UNIFRAKTUR_MAGUNTIA`) — never rename or remove a constant. `AdventureFont.cssFontFamily()` is empty for `DEFAULT` (no override). Documents saved before the field existed have no `font` and load as `DEFAULT`; `getFont()` never returns null. Applied by `AdventureRunView` as Lumo's `--lumo-font-family` on the message list and input only; the web fonts (latin subset, SIL OFL except Special Elite which is Apache 2.0, with licence texts) are bundled under `META-INF/resources/styles/fonts/` and declared in `styles/adventure-fonts.css`, loaded globally by `@StyleSheet` on `AdventureBuilderServer`. Edited with the "Run Font" `Select` in `AdventureEditorView`. |
| `worldMapPictureId` | `String`, default `null` | The id (a key of `pictureData`) of the picture the author shows as the adventure's world map, or `null` while none is chosen. A plain id like `LocationData.pictureId`, not a DBRef; a missing picture is treated as none. Edited with the "World Map" `ComboBox` in `AdventureEditorView`, shown by `LocationMapView`; `PictureUsageTracker` reports it as a usage so the picture cannot be deleted while it is the map. |
| `notes` | `String` | Free-text outline; not used at runtime. Surfaced as a quick preview in `AdventuresMenuView`'s right-click context menu. |
| `variables` | `List<VariableData>`, default empty | The variables the author has defined — embedded (owned 1:1 by the adventure, like `messages`), as a list rather than a map because variable names are free text and may be illegal as Mongo field names. `defineVariable(name)` adds one (trimmed, case-sensitive, idempotent); `variableNames()` lists them. Created when a Set Variable action names a variable, see [`07-ui-and-navigation.md`](07-ui-and-navigation.md). |
| `workflowData` | `WorkflowData`, default `new WorkflowData()` | The adventure's global commands — **Processes** (`commands`), **Responses** (`interceptorCommands`) and **Arrival Processes** (`arrivalProcesses`). Plain embedded field — no `@DBRef`, no cascade annotations (unlike every other nested collection above); it round-trips as part of the `AdventureData` document itself. See [§ Workflow](#workflow) below. |
| `builderVersion` | `String`, default `null` | The version of the adventure builder that last wrote this adventure, stamped by `AdventureService.saveAdventureData` from Spring Boot build info (the pom version). `null` means unknown. Saved games record it so `load` can warn when the adventure changed version since the save. |

Constructors initialise empty maps (including `systemMessages`) and an empty
`ItemContainerData("your pocket")`.

## Locations and the world

### Location (BO) / LocationData (DO)

`server/location/Location.java` extends the runtime `Thing`. `LocationData`
extends `ThingData`, collection `locations`.

DO fields beyond `ThingData`:

| Field | Type | Notes |
|-------|------|-------|
| `itemContainerData` | `@DBRef ItemContainerData` | Items in the location. |
| `directionsData` | `Set<DirectionData>` | Exits (embedded, not @DBRef). |
| `timesVisited` | `int` | Increments on entry. |
| `lumen` | `int`, default 50 | Light level; see `HasLight` API and `DescribeAction` semantics. |
| `pictureId` | `String`, optional | Id of the location's *default picture* (a key of `AdventureData.pictureData`); `null` = none. See [§ PictureData](#picturedata). |

BO holds `directions: Container<Direction>`, `itemContainer: Container`,
`timesVisited`, `lumen`, `pictureId` and inherits the description and command map from `Thing`.

### Direction (BO) / DirectionData (DO)

`server/location/GenericDirection.java` (BO).
`model/DirectionData.java` (DO).

DO fields:

| Field | Type | Notes |
|-------|------|-------|
| `descriptionData` | `DescriptionData` | Short and long form of the exit text. |
| `destinationId` | `String` | Target `LocationData.id`. |
| `destinationMustBeMentioned` | `boolean` | Forces the player to name the destination in the command. |
| `commandData` | `CommandData` | The single trigger command (e.g. *go north*). |

A direction owns exactly one `CommandData`; the trigger is data, not hard-coded.

## Things, containers, items

### Thing (BO) / ThingData (DO)

`server/tangible/Thing.java` is the abstract runtime base class for everything
the player can perceive. `ThingData` is the persistence shape.

DO fields:

| Field | Type | Notes |
|-------|------|-------|
| `descriptionData` | `DescriptionData` | Long & short text. |
| `commandProviderData` | `CommandProviderData` | The map of commands keyed by description. |

### CommandProviderData / CommandChainData

A `CommandProviderData` is a `Map<String, CommandChainData>` where the key is a
**stable ULID chain ID** (not a derived command-spec string). This ensures that
command identity persists across edits to the verb, adjective, or noun descriptions.
A `CommandChainData` holds the ordered list of `CommandData` that all match that
chain — the engine evaluates them in order until one's PreConditions pass.

When matching commands at runtime, `GenericCommandProvider` filters chains by the
parsed `CommandDescription` (verb, adjective, noun) after retrieving them from the
map, and supports **empty noun wildcard matching** where a noun-less command can be
authored to apply to any item (e.g., a single `examine` command for all pickables).

### Item (BO) / ItemData (DO)

`server/tangible/Item.java` (BO). `model/ItemData.java`, collection `items`,
extends `ThingData`.

DO fields beyond `ThingData`:

| Field | Type | Notes |
|-------|------|-------|
| `adventureId` | `String` | Scope. Compound index `(adventureId, locationId)`. |
| `locationId` | `String` | Scope. Indexed alongside `adventureId`. |
| `isContainable` | `boolean` | Can be placed in a container. |
| `parentContainerId` | `String` | Current parent. Empty/null when in a location. |
| `isWearable` | `boolean` | Can be worn. |
| `isWorn` | `boolean` | Is currently worn. Implies carried. While worn, the item's short, long **and** enriched-short descriptions get the `SystemMessageKey.SM10` marker (default `" (worn)"`) appended by `Item`. |

Items are stored in their **own collection** (not embedded in locations) so
that ownership transfer is a single document update.

### ItemContainer (BO) / ItemContainerData (DO)

`server/tangible/GenericContainer.java` (BO). `model/ItemContainerData.java`,
collection `containers`, extends `ItemData` (a container *is* a wearable/
containable item).

DO fields beyond `ItemData`:

| Field | Type | Notes |
|-------|------|-------|
| `items` | `@DBRef(lazy=false) List<ItemData>` | Contents. Cascade save & delete. |
| `maxSize` | `int`, default 10 | Capacity; exceeding raises `ContainerFullException`. |
| `holdingDirections` | `boolean` | Used for the special location-direction container. |

## Vocabulary, words, command descriptions

### Word

`model/Word.java`, collection `words`, extends `DatedData`. Used **directly** as
both DO and BO.

| Field | Type | Notes |
|-------|------|-------|
| `text` | `String` | Lower-cased on construction. |
| `type` | `Word.Type` | `VERB` / `NOUN` / `ADJECTIVE` / `CONJUNCTION` / `PRONOUN`. The last two are special-purpose, author-created: `CONJUNCTION` (`and`, and its synonym `then`) marks a sub-command boundary in the parser; `PRONOUN` (`it`) resolves to the last-mentioned noun+adjective. The engine seeds neither; they come from the adventure's vocabulary. |
| `synonym` | `@DBRef Word` | Optional reference to a canonical word. |

When constructed from another word with the *synonym* constructor, the new word
adopts the existing synonym chain (no transitive references); when no synonym is
supplied, `synonym = null`. Synonyms MUST resolve in finitely many hops (no cycles).

### Vocabulary (BO) / VocabularyData (DO)

`server/vocabulary/Vocabulary.java` (BO). `model/VocabularyData.java`,
collection `vocabularies`, extends `DatedData`.

DO fields:

| Field | Type | Notes |
|-------|------|-------|
| `words` | `@DBRef(lazy=false) Map<String, Word>` | All words keyed by text. Cascade save & delete. |

The class also exposes a comprehensive list of **string constants** for UI
labels (e.g. `BACK_TEXT`, `SAVE_TEXT`, `UNKNOWN_WORD_TEXT`). Centralising these
makes the domain text translatable in one place.

Key methods:

| Method | Returns |
|--------|---------|
| `ensureWildcardNoun()` | The `Word` `"~"` (`WILDCARD_NOUN`, type `NOUN`), creating it if absent; an existing same-text word is returned untouched (never retyped or repointed). Called by the Response editor so a Response can be keyed on "any noun". |
| `findWordsBySynonym(Word aTarget)` | `List<Word>` of all words whose `synonym` field points to `aTarget` (excludes `aTarget` itself). Used by `WordEditorDialogue` to detect cascade-affected words when a synonym is reassigned. |

### CommandDescriptionData

`model/basic/CommandDescriptionData.java` extends `BasicDescriptionData`
(noun/adjective slots) and adds a `verb: @DBRef Word`. Has helpers:

- `getCommandSpecification(): String` — joins verb / adjective / noun with
  `CommandDescription.COMMAND_SEPARATOR`.
- `setCommandSpecification(String)` — splits the spec back into the three slots
  (currently bypasses the vocabulary; flagged with a TODO in source).

Two `CommandDescriptionData` are equal iff their command specifications are
equal — that is the key for command-chain lookup.

## Commands and chains

### CommandData

`model/CommandData.java` extends `BasicData`. A leaf command:

| Field | Type |
|-------|------|
| `commandDescription` | `CommandDescriptionData` |
| `preConditions` | `List<PreConditionData>` |
| `actions` | `List<ActionData>` — a single ordered list; all actions in it run in sequence when the command fires. There is no primary-action/follow-up-actions split at the data level (`CommandData.addAction(ActionData)` appends). |

### CommandChainData

`model/CommandChainData.java` extends `BasicData`. Ordered list of `CommandData`
sharing the same `commandSpecification` key. The engine walks the list in
order and runs the first command whose PreConditions all pass.

### Workflow

`model/WorkflowData.java` — a minimal `@Data` POJO, **not** a `BasicData`
subclass and not its own MongoDB collection:

```java
public class WorkflowData {
    private List<CommandData> commands = new ArrayList<>();             // Processes
    private List<CommandData> interceptorCommands = new ArrayList<>();  // Responses
    private List<CommandData> arrivalProcesses = new ArrayList<>();     // Arrival Processes
}
```

It lives as a plain embedded field on `AdventureData.workflowData` (see the
field table above) and holds the adventure's *global* commands — built from
the identical `CommandData` shape as location/item commands, but not scoped
to one `Thing`. Three independent lists:

- **`commands` — Processes.** Run automatically before **each parsed
  sub-command** of a turn, before that sub-command is dispatched. The verb
  is not required.
- **`interceptorCommands` — Responses.** Tried only as a **fallback**, after
  pocket/location dispatch has failed to match the verb at all. An exact
  `(verb, adjective, noun)` match that a location or item command also
  carries loses to that local command. The verb is required. The noun may be
  the wildcard `~` (`VocabularyData.WILDCARD_NOUN`, a persisted NOUN `Word`
  created on demand by `ensureWildcardNoun()`), which matches any typed noun
  or none — see [`04-runtime-engine.md` § Wildcard noun](04-runtime-engine.md#wildcard-noun-). (This
  *persisted* field name predates the engine rename — the runtime table is
  now `Workflow.responses`.)
- **`arrivalProcesses` — Arrival Processes.** Run automatically whenever the
  current location's description is (re)shown: after a `MovePlayerAction`
  walk-in (their output follows the destination's arrival description) and on
  an explicit look / describe (`LookAction`, in the author's
  `describe` Response). They re-fire on **every** redescribe by
  design — an author who wants "only once" adds a guard condition. The list is
  adventure-wide, not per location: gate an entry with a *player is at*
  precondition. The verb is not required — verb / adjective / noun are not
  matched against the player's input and only label the entry. See
  [`04-runtime-engine.md` § Workflow](04-runtime-engine.md#workflow-processes-and-responses).

At runtime, `WorkflowMapper.populate(WorkflowData, Workflow)` layers `commands`
onto the engine `Workflow` (`server/engine/Workflow.java`) via `addProcess`,
`interceptorCommands` via `addResponse` and `arrivalProcesses` via
`addArrivalProcess`, after
`GameContext.setUpWorkflows()`; the engine plants no built-in entries — see
[`04-runtime-engine.md` § Workflow](04-runtime-engine.md#workflow-processes-and-responses).
Authored via `WorkflowEditorView` (`author/adventures/:adventureId/workflow`,
Processes) and `ResponsesEditorView`
(`author/adventures/:adventureId/responses`, Responses) and
`ArrivalProcessesEditorView` (`author/adventures/:adventureId/arrival`, Arrival
Processes), all built on the shared `CommandListEditorView`.

## Pictures

### PictureData

`model/PictureData.java`, `@Document(collection = "pictures")`, extends
`BasicData` (a ULID `id`; no timestamps). One document per uploaded image,
owned by one adventure and referenced from `AdventureData.pictureData`.

| Field | Type | Notes |
|-------|------|-------|
| `adventureId` | `String` | The owning adventure (set by `PictureEditorView`). |
| `name` | `String` | Author-chosen label; required; shown in lists and pickers. Need not be unique. |
| `content` | `byte[]` | The image bytes (stored inline in the document). |
| `contentType` | `String` | `image/png`, `image/jpeg` or `image/webp` — sniffed from the bytes' magic numbers at upload, never taken from the file name. |

Uploads are capped at 2 MB. A picture is referenced by its `id` from
`LocationData.pictureId` (the location's *default picture*) and from
`PictureActionData.pictureId`; nothing else holds one. At runtime the current
picture is `GameContext.currentPictureId` — see
[`04-runtime-engine.md` § Pictures](04-runtime-engine.md#pictures).

## Messages, variables, IO

### MessageData

`model/MessageData.java`, extends `DatedData`. Embedded in `AdventureData.messages`, a map keyed by the
message's `id` — not a collection or document of its own.

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` | Inherited from `BasicData` (a ULID, assigned on creation). The map key and the **only** reference to a message: `MessageActionData.messageId` holds it. Never edited. |
| `summary` | `String` | A short, free-text label of what the message says, shown to authors in lists and pickers. Not a reference and not required to be unique. |
| `text` | `String` | The message body. |
| `font` | `AdventureFont`, default `DEFAULT` | The font this message is shown in when the adventure runs, stored as the constant's **name** (same stability rule as `AdventureData.font`). `DEFAULT` means "no override": the adventure's own `font` applies. Absent or null loads as `DEFAULT`; `getFont()` never returns null. Edited with the "Message Font" select in `MessageEditorView`. |

### MessagesHolder

`server/storage/message/MessagesHolder.java` is the runtime cache of messages
keyed by message id. `MessageAction` looks up text here. Text and font are one entry per id
(`getMessage`, `getFont`; an unknown id has `DEFAULT`), filled by `LoadAdventureAction`.

### Message fonts at run time (`FontMarkup`)

Actions don't print: each returns a result message, a command's (or chain's) messages are joined into one
string, and `GameLoop` prints that string once per turn. So a message's font has to travel inside the text.
`MessageAction` (built by `MessageActionMapper` from the holder's text **and** font) wraps its text with
`FontMarkup.wrap(text, font)` — `START <font name> SEP <text> END`, three Unicode private-use characters
(`U+E000`–`U+E002`; control characters would be removed by the `String.trim()` in `Workflow`). `DEFAULT`
and blank text are never wrapped, so adventures without message fonts produce byte-identical output.

`AdventureRunView.renderNarratorLines` runs `FontMarkup.split` on the turn's text (both the per-turn lines and
the opening room): text without markers is one message as before; with markers, each font run becomes its own
`MessageListItem` tagged with the font's class (`AdventureFont.cssClassName()`, e.g. `run-font-special-elite`),
which `styles/adventure-fonts.css` maps to the font on `vaadin-message`. The console's default sink uses
`FontMarkup.strip`; a browser session's sink keeps the markers. Unknown font names (a retired font) fall back to
`DEFAULT`. Only `MessageAction` text gets a font — location/item descriptions and system messages do not.

### System messages

Two collaborating types back the "Manage System Messages" screen:

- **`server/storage/message/SystemMessageKey.java`** — a Java `enum`, the
  fixed catalog of built-in engine text. Each constant carries an id (kept
  verbatim for entries that already used `MessagesHolder`'s
  negative-id convention, so a future engine rewiring needs no id remap), a
  default English `text`, a source location, and a translator-facing
  description. Helpers: `defaultText()`, `id()`, `sourceLocation()`,
  `description()`, `fromId(String)`. Engine code that used to hold raw
  literals now calls `SystemMessageKey.SMnn.defaultText().formatted(...)` —
  see [`04-runtime-engine.md`](04-runtime-engine.md#action-catalog).
- **`model/SystemMessageData.java`** — extends `DatedData`, no collection of
  its own. Stores only the mutable text for one catalog `key`. Storage is
  **sparse**: an entry is written the first time an adventure edits a key away
  from its default; an unedited key has no entry. Embedded in
  `AdventureData.systemMessages` (a plain map — no `@DBRef`, no cascade), so
  the overrides are per adventure by construction.

`server/support/PlaceholderSpec.java` validates that an edited override keeps
exactly the `%s` / `%n$s` argument positions of the catalog default, so a
reworded message can't trigger a `MissingFormatArgumentException` at runtime.
The catalog is fixed — the screen can edit an entry but never create, delete,
or rename one.

### VariableData

`model/VariableData.java`, extends `BasicData`, embedded in `AdventureData.variables` (no collection
of its own). Fields: `name` (the identity — `equals`/`hashCode` use it only) and `initialValue`
(`int`, default 0; the value a game starts with — not yet editable in the UI). `VariableMapper`
(`server/mapper/`) maps it to the runtime `Variable` and back.

### VariableProvider / Variable

`server/support/VariableProvider.java`, `Variable.java`. Holds named
variables consumed by the `*VariableAction` and `*VariableCondition` families.
Variables must be defined or added first through the SetVariableAction before they 
can be used in any other action or condition. Propper mapping between the `VariableProvider` and
the `VariableData` is available trhough the `VariableMapper`.

## Action and PreCondition data

Concrete `ActionData` and `PreConditionData` subclasses live in
`model/action/` and `model/condition/`, one DO per runtime kind. The engine's
behavioural catalog is the subject of [`04-runtime-engine.md`](04-runtime-engine.md);
this chapter only documents the storage shape.

| ActionData | Maps to |
|------------|---------|
| `CreateActionData`, `DestroyActionData` | `CreateAction` (add to a container), `DestroyAction` (remove from its parent container) — both authorable via the Action editor ("Create Item" / "Destroy") |
| `DescribeActionData` | `DescribeAction` |
| `PictureActionData` | `PictureAction` (`pictureId` — a key of `AdventureData.pictureData`) |
| `DropActionData`, `TakeActionData`, `WearActionData`, `RemoveActionData` | inventory-handling actions |
| `AutoTakeActionData`, `AutoDropActionData`, `AutoWearActionData`, `AutoRemoveActionData` | AutoTake / AutoDrop / AutoWear / AutoRemove — parameterless; the item is resolved at runtime from the typed noun (see [`04-runtime-engine.md` § Auto item actions](04-runtime-engine.md#auto-item-actions-autotake-autodrop-autowear-autoremove)) |
| `MovePlayerActionData`, `MoveItemActionData` | spatial actions |
| `MessageActionData` | text emission |
| `InventoryActionData` | print pocket |
| `QuitActionData` | end the session (`QuitAction`) |
| `BreakActionData` | stop the rest of the current command chain / action list (`BreakAction`) |
| `SetVariableActionData`, `IncrementVariableActionData`, `DecrementVariableActionData` | variable mutations |

Plus a runtime-only `LoadAdventureAction` (no DO; engine-managed).

| PreConditionData | Maps to |
|------------------|---------|
| `CarriedConditionData`, `WornConditionData`, `HereConditionData`, `ItemAtConditionData`, `PlayerAtConditionData` | item / location predicates |
| `ChanceConditionData` | random-roll gate (`ChanceCondition`) |
| `EqualsConditionData`, `GreaterThanConditionData`, `LessThanConditionData`, `SameConditionData` | variable comparisons |
| `NotConditionData` | composite — wraps another `PreConditionData` and inverts its result. Applied in the UI via a per-condition **Negate** checkbox (`ConditionRow.toConditionData()`) rather than being one of the directly-selectable condition kinds. |

## Security / access control entities

`security/model/` contains the JPA entities that bridge user identity to
adventure ownership and access. The fields are detailed in
[`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#mysql-schema)
and the access rules in
[`06-security-and-access-control.md`](06-security-and-access-control.md).

| Entity | Table | Identity |
|--------|-------|----------|
| `UserData` (`UserDetails`) | `users` | ULID `id`; unique `username`. `roles` is an `@ElementCollection` of `Role` enum strings. |
| `Role` (enum) | — | `ADMIN`, `AUTHOR`, `PLAYER`. |
| `AdventureAuthor` | `adventure_authors` | `adventureId` is the primary key; one author per adventure. |
| `AdventurePlayer` | `adventure_players` | Composite PK `AdventurePlayerId(adventureId, userId)`. |

## Domain diagram

```
Adventure (BO)
├── Vocabulary           (wraps VocabularyData → words, special-verb slots)
├── MessagesHolder       (runtime cache of MessageData)
├── VariableProvider     (named runtime variables)
├── pocket: Container    (the player's GenericContainer)
├── Workflow             (Processes + Responses, from AdventureData.workflowData)
├── (system messages)    (SystemMessageData overrides of the SystemMessageKey catalog)
└── locationMap: Map<String, Location>
     └── Location  (Thing + directions + items + lumen)
          ├── ItemContainer
          │    └── Item[] (Containable, Wearable)
          ├── Direction[]   (Description + destinationId + Command)
          └── CommandChain[] (CommandDescription + Command[], each Command =
                               PreCondition[] + Action[], tried in order)

Cross-store
├── (MySQL) UserData (Spring Security UserDetails)
├── (MySQL) AdventureAuthor (adventureId PK → UserData)
└── (MySQL) AdventurePlayer (PK (adventureId,userId) → UserData)
```

## Lifecycle and ownership invariants

1. **Adventure-owned cascade.** Saving an `AdventureData` cascades to
   `playerPocket`, `locationData` and `vocabularyData` via the
   custom `@CascadeSave` machinery. Deleting cascades likewise via
   `@CascadeDelete` (see [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#cascade-save)).
2. **Item scoping.** An `ItemData` is uniquely identified by its `id`, but
   queries that list items at a location MUST filter by `(adventureId, locationId)`
   — the compound index supports this.
3. **Vocabulary uniqueness.** Two `Word` documents with the same `(text, type)`
   cannot coexist in one adventure's `VocabularyData.words`.
4. **Special-word completeness.** Before play begins, every special-word slot on
   `VocabularyData` MUST resolve to a non-null `Word`.
5. **Container capacity.** Adding to a container at `maxSize` raises
   `ContainerFullException`. Putting a non-`Containable` raises
   `NotContainableException`.
6. **Worn ⇒ Carried.** An item with `isWorn = true` MUST be in the player's
   pocket (or one of its sub-containers). Drop-while-worn auto-runs `RemoveAction`.
7. **Pictures are saved explicitly and deleted by cascade.** `AdventureData.pictureData` carries
   `@CascadeDelete` but not `@CascadeSave`: deleting an adventure removes its `pictures`
   documents, while creating or changing one goes through `PictureEditorView` →
   `AdventureService.savePictureData` (then the adventure is saved).

## Source pointers

- `src/main/java/com/pdg/adventure/api/` — `Action`, `PreCondition`, `Command`,
  `CommandDescription`, `Container`, `Containable`, `Wearable`, `Direction`,
  `Visitable`, `HasLight`, `HasCommands`, `Describable`, `Ided`, `Mapper`,
  `ExecutionResult`.
- `src/main/java/com/pdg/adventure/model/basic/` — `BasicData`, `DatedData`,
  `BasicDescriptionData`, `DescriptionData`, `CommandDescriptionData`.
- `src/main/java/com/pdg/adventure/model/` — `AdventureData`, `LocationData`,
  `ItemData`, `ItemContainerData`, `DirectionData`, `CommandData`,
  `CommandChainData`, `CommandProviderData`, `MessageData`,
  `SystemMessageData`, `WorkflowData`, `VocabularyData`, `Word`, `ThingData`,
  `SavedGameData` (a saved game, collection `savedgames`) and `GameSnapshotData`
  (its embedded runtime state).
- `src/main/java/com/pdg/adventure/model/action/` — every `*ActionData`
  (incl. `BreakActionData`).
- `src/main/java/com/pdg/adventure/model/condition/` — every `*ConditionData`.
- `src/main/java/com/pdg/adventure/server/Adventure.java`,
  `server/location/{Location,GenericDirection}.java`,
  `server/tangible/{Thing,Item,GenericContainer,ItemIdentifier}.java`,
  `server/vocabulary/Vocabulary.java`,
  `server/storage/message/{MessagesHolder,SystemMessageKey}.java`,
  `server/support/{Variable,VariableProvider,DescriptionProvider,ArticleProvider,PlaceholderSpec}.java`.
- `src/main/java/com/pdg/adventure/security/model/` — `UserData`, `Role`,
  `AdventureAuthor`, `AdventurePlayer`, `AdventurePlayerId`.

## Known gaps

- **`CommandDescriptionData.setCommandSpecification` bypasses the vocabulary**
  (`CommandDescriptionData.java:57`). The TODO in source flags this; a rebuild
  SHOULD route every word creation through `Vocabulary` so synonym and
  duplicate rules apply uniformly.
- **`Variable` state is saved with a saved game** (`GameSnapshotData.variables`); it
  is otherwise in memory only. The `saveWord` / `loadWord` slots on `VocabularyData`
  are not used by the engine: the author binds `save ~` / `load ~` Responses instead.
- **`ItemContainerData.holdingDirections` flag is not consistently used.** It
  hints at a planned use (a container that holds directions) that is currently
  inactive.
