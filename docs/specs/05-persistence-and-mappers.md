# 05 — Persistence and Mappers

## Purpose

This chapter documents the **two-database design** (MongoDB for game content,
MySQL for user identity and access), the **cascade-save / cascade-delete**
custom machinery for MongoDB `@DBRef`, the **mapper subsystem** that translates
DOs to BOs at runtime, and the **service / repository layer** sitting on top.

It complements [`03-domain-model.md`](03-domain-model.md), which describes
*what* is stored, by explaining *how* it is stored, retrieved, and translated.

## Two-database split

`com.pdg.adventure.config.DatabaseConfig` registers both Spring Data stacks in
a single configuration class:

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.pdg.adventure.server.security.repository")  // MySQL
@EnableMongoRepositories(basePackages = "com.pdg.adventure.server.storage.repository")  // MongoDB
public class DatabaseConfig { }
```

| Concern | Store | Reason |
|---------|-------|--------|
| Authentication, role assignment, adventure ownership, adventure player access | MySQL via Spring Data JPA | Strong relational consistency, `UserDetails` integrates naturally with Spring Security. |
| Adventures, locations, items, vocabulary, words, messages, containers | MongoDB via Spring Data MongoDB | Free-form, deeply nested aggregates; cheap document-level updates. |

A single `adventureId` (a 26-char ULID) bridges the two: it is the primary key
of `AdventureData` in MongoDB and also stored as a string column in
`adventure_authors.adventure_id` and `adventure_players.adventure_id`.

## MongoDB schema

### Collections

| Collection | Document class | Notes |
|------------|----------------|-------|
| `adventures` | `AdventureData` | Root aggregate. References pocket, locations, vocabulary, messages by `@DBRef` with cascade. |
| `locations` | `LocationData` | Individual locations; references one `ItemContainerData`. |
| `containers` | `ItemContainerData` | Containers (incl. the player's pocket and per-location containers). References items by `@DBRef`. |
| `items` | `ItemData` | Items, scoped per `(adventureId, locationId)` via compound index. |
| `vocabularies` | `VocabularyData` | One per adventure. References `Word` documents and the special-word slots. |
| `words` | `Word` | Vocabulary entries. Synonyms link via `@DBRef`. |
| `messages` | `MessageData` | Reusable text. Compound unique index on `(adventureId, messageId)`. |

### Index conventions

| Index | Definition | Where defined |
|-------|------------|---------------|
| `adventure_location_item_idx` (`ItemData`) | `{adventureId: 1, locationId: 1}` | `ItemData.@CompoundIndex` |
| `adventure_message_idx` (`MessageData`) | `{adventureId: 1, messageId: 1}` UNIQUE | `MessageData.@CompoundIndex` |

Add additional indexes as queries demand; today the codebase relies on `@Id`
lookups and these two compounds.

### Auditing

`com.pdg.adventure.server.storage.mongo.MongoDbConfig` enables
`@EnableMongoAuditing`. Every `DatedData` document therefore receives
`@CreatedDate` and `@LastModifiedDate` automatically.

### ID generation

`UuidIdGenerationMongoEventListener` (annotated `@Order(HIGHEST_PRECEDENCE)`)
intercepts `BeforeConvertEvent<Ided>` and assigns a fresh ULID if the document
has no id. This safety-net runs *before* the cascade listener so cascaded
children are saveable even if a caller forgot to populate their ids.

### Cascade save

`CascadeSaveMongoEventListener` (in `server/storage/mongo/`) walks every field
annotated with `@CascadeSave` (single object, `Map<?, ?>`, or `Collection<?>`)
on a `BeforeConvertEvent` and calls `mongoTemplate.save(child)` recursively
before saving the parent. This emulates `cascade=PERSIST` from JPA on top of
`@DBRef` (which Spring Data MongoDB does **not** cascade by default).

`@CascadeSave` is currently applied to:

| Parent → child field | Effect |
|----------------------|--------|
| `AdventureData.playerPocket` (`@DBRef(lazy=true)`) | Saves the pocket container before the adventure. |
| `AdventureData.locationData` (`@DBRef(lazy=false)`) — Map | Saves every location. |
| `AdventureData.vocabularyData` (`@DBRef(lazy=false), transient`) | Saves the vocabulary. |
| `AdventureData.messages` (`@DBRef(lazy=true)`) — Map | Saves every message document. |
| `LocationData.itemContainerData` (`@DBRef(lazy=false)`) | Saves the location's container. |
| `ItemContainerData.items` (`@DBRef(lazy=false)`) — List | Saves every contained item. |
| `VocabularyData.words` (`@DBRef(lazy=false)`) — Map | Saves every word. |

### Cascade delete

`CascadeDeleteHelper` (a `@Component`) walks `@CascadeDelete` fields
recursively and removes the referenced documents from their respective
collections. Unlike save, delete is invoked **explicitly** from service
methods (`AdventureService.deleteAdventure`) before the parent itself is
removed; it is not an event-driven listener. The helper uses
`MongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), childClass)`.

`@CascadeDelete` applies to the same fields as `@CascadeSave` listed above,
giving Adventure root-deletion these effects:

```
delete adventures/<id>
  ├── delete containers/<pocketId>          (and nested items)
  ├── delete locations/<each>                (and each location's container & items)
  ├── delete vocabularies/<id>               (and every word in it)
  └── delete messages/<each>
```

### Word lifecycle

Words are first-class documents because synonym chains and reuse across
commands require sharing. Deleting the adventure's vocabulary also deletes
the words via cascade; **author-driven word deletion** (via the editor) is
refused while any command, item, location, or direction references the word
(see [`02-functional-requirements.md` § B9](02-functional-requirements.md#b9-manage-vocabulary)).

## MySQL schema

Schema is generated by Hibernate today (`spring.jpa.hibernate.ddl-auto=update`)
because no Flyway migrations exist yet. The expected tables are:

### `users`

| Column | Type | Notes |
|--------|------|-------|
| `id` | CHAR(26) PK | ULID, generated by `UserData.@PrePersist`. |
| `username` | VARCHAR, NOT NULL, UNIQUE | |
| `password` | VARCHAR, NOT NULL | BCrypt-encoded. |
| `is_enabled` | BOOLEAN | Backs `UserDetails.isEnabled()`. |

Plus `users_roles` (the JPA `@ElementCollection`):

| Column | Type | Notes |
|--------|------|-------|
| `user_data_id` | CHAR(26) FK → `users.id` | Hibernate-derived name. |
| `roles` | VARCHAR | `Role` enum stored as STRING (`@Enumerated(EnumType.STRING)`). |

### `adventure_authors`

| Column | Type | Notes |
|--------|------|-------|
| `adventure_id` | CHAR(26) PK | The MongoDB ULID; one row per adventure enforces one-author-per-adventure. |
| `user_id` | CHAR(26) FK → `users.id`, NOT NULL | EAGER `@ManyToOne`. |

### `adventure_players`

Composite PK `(adventure_id, user_id)`:

| Column | Type | Notes |
|--------|------|-------|
| `adventure_id` | CHAR(26) | Part of `AdventurePlayerId`. |
| `user_id` | CHAR(26) FK → `users.id`, NOT NULL | EAGER `@ManyToOne` `@MapsId("userId")`. |

### MySQL relationships

- `AdventureAuthor.user` is an EAGER `@ManyToOne(optional=false)`. Deleting
  a `UserData` that is referenced by any `AdventureAuthor` row MUST be
  prevented by the application (or cascade to `RESTRICT`); otherwise Hibernate
  raises a constraint violation. The current code does not enforce this — see
  [Known gaps](#known-gaps).
- `AdventurePlayer.user` likewise is `@ManyToOne(optional=false)` with `@MapsId`.
- The `AdventureAuthor.adventureId` column is **not** a foreign key (the
  referenced row lives in MongoDB). Application code keeps the two stores in
  sync; there is no DB-level integrity.

### Connection settings

`src/main/resources/application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/adventure_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=adventure_user
spring.datasource.password=adventure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

The "create database" SQL is documented (commented) at the bottom of
`application.properties`:

```sql
CREATE DATABASE adventure_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'adventure_user'@'%' IDENTIFIED BY 'adventure_password';
GRANT ALL PRIVILEGES ON adventure_db.* TO 'adventure_user'@'%';
```

## MongoDB connection settings

```
spring.mongodb.authentication-database=admin
spring.mongodb.database=adventures
spring.mongodb.username=advAdmin
spring.mongodb.password=example
spring.mongodb.host=localhost
spring.mongodb.port=27017
```

A bootstrap `advAdmin` user exists on the dev MongoDB (the SCRAM credentials
are recorded as a comment at the bottom of `application.properties`); the
Docker compose file runs MongoDB with root `root/example` and Mongo Express
exposes a UI at `localhost:8081`.

## Repositories

All MongoDB repositories sit in `server.storage.repository` and extend
`MongoRepository<TData, String>` (id is a `String`):

| Repository | Document | Custom methods |
|------------|----------|----------------|
| `AdventureRepository` | `AdventureData` | (Spring Data defaults) |
| `LocationRepository` | `LocationData` | (Spring Data defaults) |
| `ItemRepository` | `ItemData` | `findByAdventureIdAndLocationId(...)`, `findByAdventureIdAndId(...)`, etc. (named-method derivations) |
| `MessageRepository` | `MessageData` | `existsByAdventureIdAndMessageId(...)`, `findByAdventureId(...)` |
| `VocabularyReporitory` | `VocabularyData` | (Spring Data defaults) — note the typo'd class name; fix on rebuild. |
| `WordRepository` | `Word` | (Spring Data defaults) |

All MySQL repositories sit in `server.security.repository` and extend
`JpaRepository<TEntity, TId>`:

| Repository | Entity | Custom methods |
|------------|--------|----------------|
| `UserRepository` | `UserData` (id `String`) | `findByUsername(String)`, `findByRolesContaining(Role)` |
| `AdventureAuthorRepository` | `AdventureAuthor` (id `String`) | `findByAdventureId`, `findByUser(UserData)`, `existsByAdventureId`, `deleteByAdventureId` |
| `AdventurePlayerRepository` | `AdventurePlayer` (id `AdventurePlayerId`) | `findByIdAdventureId`, `findByUser`, `existsByIdAdventureIdAndUser`, `deleteByIdAdventureId`, `deleteByIdAdventureIdAndUser` |

Repositories MUST return `Optional<T>` for single-id lookups — services rely
on this contract and never observe `null`.

## Services

### MongoDB-side

| Service | Responsibility |
|---------|---------------|
| `AdventureService` | Top-level CRUD over adventures, locations, vocabulary, words. Calls `CascadeDeleteHelper` before deleting an adventure. Saves emit DEBUG / INFO logs. Pre/post-process hooks (`preProcess`, `postProcess`) are present but currently empty (the previous logic is commented out, awaiting redesign). |
| `ItemService` | Item CRUD scoped by `(adventureId, locationId)`. `@Transactional updateItem(...)` reads the existing item then saves. |
| `MessageService` | Message CRUD with uniqueness check on `(adventureId, messageId)` raising `IllegalArgumentException` on conflict. |

`DataManager` exists in `server.storage` but is a thin wrapper / transitional
class; new code should call the typed services above.

### MySQL-side

| Service | Responsibility |
|---------|---------------|
| `CustomUserDetailsService` | Implements `UserDetailsService.loadUserByUsername`; throws `UsernameNotFoundException` on miss. |
| `UserService` | User CRUD + role assignment; enforces username uniqueness; encodes passwords via `PasswordEncoder`. |
| `AdventureAccessService` | The cross-store coordinator. Read/write checks (`canRead`, `canWrite`); creates an adventure in MongoDB **then** writes the `AdventureAuthor` row in MySQL, all inside `@Transactional`. Lists adventures visible to a given user (ADMIN sees all; AUTHOR sees authored; PLAYER sees assigned). |

### Cross-store consistency

The application accepts a small inconsistency window: MongoDB writes happen
*before* the MySQL `@Transactional` block commits. If MySQL fails, the
adventure document remains orphaned. A rebuild MAY tighten this with a
saga-style compensation (delete the document on MySQL rollback) or accept the
current pattern; either way, document the trade-off.

## Mapper subsystem

The mapper subsystem translates between `*Data` documents and runtime BOs.

### `Mapper<DO, BO>` interface

```java
public interface Mapper<DO, BO> {
    BO mapToBO(DO from);
    DO mapToDO(BO from);
    default List<BO> mapToBOs(List<DO> dataObjectList) { … }
    default List<DO> mapToDOs(List<BO> businessObjectList) { … }
}
```

(`api/Mapper.java`.)

### `@AutoRegisterMapper`

```java
@Target(TYPE)
@Retention(RUNTIME)
public @interface AutoRegisterMapper {
    int priority() default 100;     // lower = earlier
    String description() default "";
}
```

(`server/annotation/AutoRegisterMapper.java`.) The `@RegisterMapper`
annotation that some older docs reference has been **deleted** and MUST NOT be
re-introduced.

### Registration: a single BeanPostProcessor

Contrary to one or two stale notes in `AGENTS-architecture.md`, the
registration is performed by **one** `BeanPostProcessor`:

`AutoMapperRegistrationProcessor` (`@Component @Order(1001)`) implements
`BeanPostProcessor` with both pre- and post-init callbacks:

1. `postProcessAfterInitialization(bean, name)` — when a bean carrying
   `@AutoRegisterMapper` is initialised, queue a `PendingAutoRegistration`
   record (DO class, BO class, mapper instance, priority, name, description).
   Generic types are resolved with Spring's `GenericTypeResolver`, falling
   back to a manual `ParameterizedType` walk.
2. `postProcessBeforeInitialization(bean, name)` — when the
   `MapperSupporter` bean is about to be initialised, sort the queue by
   priority and call `mapperSupporter.registerMapper(do, bo, mapper)` for
   each. The queue is then cleared.

`MapperSupporter` itself is a plain `@Service`. It exposes:

- `vocabulary`, `variableProvider`, `messagesHolder`,
  `mappedItems`, `mappedLocations`, `mappedContainers` — runtime caches
  populated during mapping.
- `registerMapper(DO, BO, Mapper)` — stores the mapper under both keys.
- `getMapper(Class<?>)` — typed lookup.
- `addMappedLocation / Item / Container`, `getMappedLocation / Item / Container`
  — registries used by mappers to share already-translated runtime objects
  (so `LocationData.directions[i].destinationId` can be resolved into a real
  `Location` BO already in flight).

### Mapper inventory

`server/mapper/`:

| Mapper | Translates | Notes |
|--------|-----------|-------|
| `AdventureMapper` | `AdventureData` ↔ `Adventure` | Top-level. Priority 100. Composes the vocabulary, location, container mappers. Manually registers itself in its constructor (a belt-and-braces alongside the auto-registration). |
| `VocabularyMapper` | `VocabularyData` ↔ `Vocabulary` | Wraps the data and exposes BO methods. |
| `LocationMapper` | `LocationData` ↔ `Location` | Builds directions, item containers, command map. |
| `DirectionMapper` | `DirectionData` ↔ `Direction` (BO `GenericDirection`) | |
| `ItemMapper` | `ItemData` ↔ `Item` | |
| `ItemContainerMapper` | `ItemContainerData` ↔ `GenericContainer` | |
| `ThingMapper` | `ThingData` ↔ `Thing` | Description + command provider. |
| `CommandMapper` | `CommandData` ↔ `Command` | Stub today (see Known gaps). |
| `CommandChainMapper` | `CommandChainData` ↔ `CommandChain` | |
| `CommandProviderMapper` | `CommandProviderData` ↔ `GenericCommandProvider` | |
| `CommandDescriptionMapper` | `CommandDescriptionData` ↔ `GenericCommandDescription` | |
| `DescriptionMapper` | `DescriptionData` ↔ runtime description |  |

Plus `server/mapper/action/` and `server/mapper/condition/` with one mapper
per concrete `*ActionData` / `*ConditionData`.

### When mapping happens

- **Boot / play.** `AdventureMapper.mapToBO` is the entry point.
  `MiniAdventure.setup` calls
  `adventureMapper.mapToBO(adventureService.findAdventureById(id).get())`.
- **Save in editor views.** The Vaadin views work mostly in DO-space (binding
  to `*Data` or `*ViewModel`); mapping to BO is reserved for runtime play.
- **CLI run.** `AdventureClient` / `MiniAdventure` route everything through
  the mapper layer.

### Mapper-style guidelines (canonical)

1. **One mapper per DO/BO pair.** Annotate with
   `@Component` (or `@Service`) and `@AutoRegisterMapper(priority, description)`.
2. **Generic parameters** are `<DO, BO>`, in that order — the auto-registration
   relies on this.
3. **Priority** defaults to 100; lower numbers run earlier. Adjust only when
   ordering matters (e.g., `AdventureMapper` is 100; child mappers can use
   100 too because they are looked up by class, not by index).
4. **Immutability** — both directions return a new object; no in-place mutation
   of the input. The `MapperSupporter` mapped-* registries cache outputs to
   preserve identity within a single mapping pass.

## Saving and deleting an Adventure end-to-end

The intended write path:

```
AuthorEditorView.save()
   ↓
AdventureAccessService.createAdventure(data, currentUser)            ← @Transactional
   ↓
AdventureService.saveAdventureData(data)
   ↓ (Mongo lifecycle)
   • UuidIdGenerationMongoEventListener fills missing ids
   • CascadeSaveMongoEventListener saves children: pocket, locations, vocabulary, messages
   ↓
AdventureRepository.save(data)
   ↓
AdventureAccessService inserts AdventureAuthor(adventureId, currentUser)
```

Delete:

```
AdventuresMenuView.delete()
   ↓
AdventureAccessService.deleteAdventure(id, currentUser)              ← @Transactional
   ↓ canWrite check
AdventureService.deleteAdventure(id)
   ↓
   • findAdventureById → CascadeDeleteHelper.cascadeDelete(data)
       (deletes children before parent)
   • adventureRepository.deleteById(id)
   ↓
AdventureAccessService deletes AdventureAuthor + AdventurePlayer rows
```

## Source pointers

- `src/main/java/com/pdg/adventure/config/DatabaseConfig.java`
- `src/main/resources/application.properties`
- `src/main/java/com/pdg/adventure/server/storage/mongo/`:
  `MongoDbConfig.java`, `UuidIdGenerationMongoEventListener.java`,
  `CascadeSave.java`, `CascadeSaveMongoEventListener.java`,
  `CascadeDelete.java`, `CascadeDeleteHelper.java`.
- `src/main/java/com/pdg/adventure/server/storage/repository/` (Mongo repos).
- `src/main/java/com/pdg/adventure/server/storage/service/` —
  `AdventureService`, `ItemService`, `MessageService`, `DataManager`.
- `src/main/java/com/pdg/adventure/server/security/repository/` (JPA repos).
- `src/main/java/com/pdg/adventure/server/security/service/` —
  `CustomUserDetailsService`, `UserService`, `AdventureAccessService`.
- `src/main/java/com/pdg/adventure/server/annotation/` —
  `AutoRegisterMapper`, `AutoMapperRegistrationProcessor`.
- `src/main/java/com/pdg/adventure/server/support/MapperSupporter.java`.
- `src/main/java/com/pdg/adventure/server/mapper/` (and the `action/`,
  `condition/` sub-folders).
- `src/main/java/com/pdg/adventure/api/Mapper.java`.

## Known gaps

- **`VocabularyReporitory` is misspelled.** The class name and import path
  carry a typo. A rebuild SHOULD fix it (and there are several call-sites to
  update in `AdventureService` etc.).
- **`CommandMapper` is incomplete.** Persisting and rehydrating commands
  end-to-end is currently partial; rebuild MUST finish DO ↔ BO conversion of
  command-description, preconditions, action, and follow-up actions.
- **`LocationMapper` destination resolution.** Following a direction by id at
  mapping time depends on a shared `MapperSupporter.mappedLocations` cache;
  the intermediate code path is partial (see TODO in `LocationMapper`).
- **`ItemContainerMapper` contents mapping.** The `items` list translation is
  partial; some round-trips lose item references.
- **No Flyway.** Schema is generated by Hibernate `update`. A rebuild SHOULD
  introduce Flyway with `V1__create_users_table.sql`,
  `V2__create_adventure_authors_table.sql`,
  `V3__create_adventure_players_table.sql`, then set
  `spring.jpa.hibernate.ddl-auto=validate`.
- **No DB-level enforcement of `UserData` ↔ `AdventureAuthor` / `AdventurePlayer`
  cascade.** Deleting a user that authors or plays an adventure throws today.
  Add explicit cascade or refusal logic in `UserService.delete`.
- **Cross-store atomicity.** `AdventureAccessService.createAdventure` writes
  Mongo before MySQL; on MySQL failure the document leaks. Either compensate
  on rollback or document the trade-off.
- **Pre/post-process hooks empty.** `AdventureService.preProcess` and
  `postProcess` have their bodies commented out. They are placeholders for
  vocabulary normalisation that needs re-implementing once `CommandMapper`
  is finished.
- **Stray `MongoTestConfiguration`** in `test/.../server/storage/` mocks
  `MongoTemplate` for some tests; it is not used by the production path.
