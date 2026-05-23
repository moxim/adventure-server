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
    └── MessageData       (+adventureId, messageId, text, category, tags, translations, notes)
└── DatedData
    └── Word              (text, type, synonym)
└── BasicData
    └── DirectionData     (+descriptionData, destinationId, destinationMustBeMentioned, commandData)
└── BasicData
    └── CommandData       (+commandDescription, preConditions, action, followUpActions)
└── BasicData
    └── CommandChainData  (+commands: List<CommandData>)
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
| `messages` | `@DBRef(lazy=true) Map<String, MessageData>` | Reusable text. Cascade save & delete. |
| `notes` | `String` | Free-text outline; not used at runtime. |

Constructors initialise empty maps and an empty `ItemContainerData("your pocket")`.

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

BO holds `directions: Container<Direction>`, `itemContainer: Container`,
`timesVisited`, `lumen` and inherits the description and command map from `Thing`.

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
canonical command-spec string (`"verb,adjective,noun"`, joined with
`CommandDescription.COMMAND_SEPARATOR`). A `CommandChainData` holds the ordered
list of `CommandData` that all match that key — the engine evaluates them in
order until one's PreConditions pass.

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
| `isWorn` | `boolean` | Is currently worn. Implies carried. |

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
| `type` | `Word.Type` | `VERB` / `NOUN` / `ADJECTIVE`. |
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
| `takeWord`, `dropWord`, `inventoryWord`, `lookWord`, `examineWord`, `goWord`, `helpWord`, `quitWord`, `saveWord`, `loadWord` | `@DBRef(lazy=false) Word` | Special-word slots. The engine reads these to know which player input means *take*, *drop*, etc. |

The class also exposes a comprehensive list of **string constants** for UI
labels (e.g. `BACK_TEXT`, `SAVE_TEXT`, `UNKNOWN_WORD_TEXT`). Centralising these
makes the domain text translatable in one place.

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
| `action` | `ActionData` (non-null after initialisation; setter rejects `null`) |
| `followUpActions` | `List<? extends ActionData>` |

### CommandChainData

`model/CommandChainData.java` extends `BasicData`. Ordered list of `CommandData`
sharing the same `commandSpecification` key. The engine walks the list in
order and runs the first command whose PreConditions all pass.

## Messages, variables, IO

### MessageData

`model/MessageData.java`, collection `messages`, extends `DatedData`.
Compound unique index on `(adventureId, messageId)`.

| Field | Type | Notes |
|-------|------|-------|
| `adventureId` | `String` | Scope. |
| `messageId` | `String` | Author-chosen logical id (e.g. `welcome_message`). |
| `text` | `String` | The message body. |
| `category` | `String` | Optional grouping. |
| `tags` | `Set<String>` | Optional metadata. |
| `translations` | `Map<String, String>` | Locale → translated text. |
| `notes` | `String` | Author notes. |

### MessagesHolder

`server/storage/message/MessagesHolder.java` is the runtime cache of messages
keyed by message id. `MessageAction` looks up text here.

### VariableProvider / Variable

`server/support/VariableProvider.java`, `Variable.java`. Holds named
variables consumed by the `*VariableAction` and `*VariableCondition` families.
Variables are **not persisted** today; they are runtime-only state created and
reset per game session.

## Action and PreCondition data

Concrete `ActionData` and `PreConditionData` subclasses live in
`model/action/` and `model/condition/`, one DO per runtime kind. The engine's
behavioural catalog is the subject of [`04-runtime-engine.md`](04-runtime-engine.md);
this chapter only documents the storage shape.

| ActionData | Maps to |
|------------|---------|
| `CreateActionData`, `DestroyActionData` | `CreateAction`, `DestroyAction` (skeletal) |
| `DescribeActionData` | `DescribeAction` |
| `DropActionData`, `TakeActionData`, `WearActionData`, `RemoveActionData` | inventory-handling actions |
| `MovePlayerActionData`, `MoveItemActionData` | spatial actions |
| `MessageActionData` | text emission |
| `InventoryActionData` | print pocket |
| `SetVariableActionData`, `IncrementVariableActionData`, `DecrementVariableActionData` | variable mutations |

Plus a runtime-only `LoadAdventureAction` (no DO; engine-managed).

| PreConditionData | Maps to |
|------------------|---------|
| `CarriedConditionData`, `WornConditionData`, `HereConditionData`, `ItemAtConditionData`, `PlayerAtConditionData` | item / location predicates |
| `EqualsConditionData`, `GreaterThanConditionData`, `LowerThanConditionData`, `SameConditionData` | variable comparisons |
| `AndConditionData`, `OrConditionData`, `NotConditionData` | composites |

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
└── locationMap: Map<String, Location>
     └── Location  (Thing + directions + items + lumen)
          ├── ItemContainer
          │    └── Item[] (Containable, Wearable)
          ├── Direction[]   (Description + destinationId + Command)
          └── Command[]     (CommandDescription + PreCondition[] + Action + followUp Action[])

Cross-store
├── (MySQL) UserData (Spring Security UserDetails)
├── (MySQL) AdventureAuthor (adventureId PK → UserData)
└── (MySQL) AdventurePlayer (PK (adventureId,userId) → UserData)
```

## Lifecycle and ownership invariants

1. **Adventure-owned cascade.** Saving an `AdventureData` cascades to
   `playerPocket`, `locationData`, `vocabularyData`, and `messages` via the
   custom `@CascadeSave` machinery. Deleting cascades likewise via
   `@CascadeDelete` (see [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#cascade)).
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

## Source pointers

- `src/main/java/com/pdg/adventure/api/` — `Action`, `PreCondition`, `Command`,
  `CommandDescription`, `Container`, `Containable`, `Wearable`, `Direction`,
  `Visitable`, `HasLight`, `HasCommands`, `Describable`, `Ided`, `Mapper`,
  `ExecutionResult`.
- `src/main/java/com/pdg/adventure/model/basic/` — `BasicData`, `DatedData`,
  `BasicDescriptionData`, `DescriptionData`, `CommandDescriptionData`.
- `src/main/java/com/pdg/adventure/model/` — `AdventureData`, `LocationData`,
  `ItemData`, `ItemContainerData`, `DirectionData`, `CommandData`,
  `CommandChainData`, `CommandProviderData`, `MessageData`, `VocabularyData`,
  `Word`, `ThingData`.
- `src/main/java/com/pdg/adventure/model/action/` — every `*ActionData`.
- `src/main/java/com/pdg/adventure/model/condition/` — every `*ConditionData`.
- `src/main/java/com/pdg/adventure/server/Adventure.java`,
  `server/location/{Location,GenericDirection}.java`,
  `server/tangible/{Thing,Item,GenericContainer,ItemIdentifier}.java`,
  `server/vocabulary/Vocabulary.java`,
  `server/storage/message/MessagesHolder.java`,
  `server/support/{Variable,VariableProvider,DescriptionProvider,ArticleProvider}.java`.
- `src/main/java/com/pdg/adventure/security/model/` — `UserData`, `Role`,
  `AdventureAuthor`, `AdventurePlayer`, `AdventurePlayerId`.

## Known gaps

- **`CommandDescriptionData.setCommandSpecification` bypasses the vocabulary**
  (`CommandDescriptionData.java:57`). The TODO in source flags this; a rebuild
  SHOULD route every word creation through `Vocabulary` so synonym and
  duplicate rules apply uniformly.
- **`Variable` state is in-memory only.** Persistence per save game is not yet
  designed. The intended `saveWord` / `loadWord` slots on `VocabularyData`
  imply a save-game story that is not implemented.
- **`MessageData.translations`, `tags`, `category`, `notes` are not exposed in
  the editor.** They exist in the document but the `MessageEditorView` only
  edits `text` and `messageId`.
- **`ItemContainerData.holdingDirections` flag is not consistently used.** It
  hints at a planned use (a container that holds directions) that is currently
  inactive.
