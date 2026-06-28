# 09 — Rebuild Blueprint

## Purpose

This chapter is the linear bootstrap order for re-creating the Adventure
Builder server module from an empty repository. Every step references the
chapter that contains the contract. It also consolidates the
**roadmap & known gaps** surfaced throughout the suite so a rebuilder does
not re-introduce the gaps unintentionally.

If you only have ten minutes, read this chapter; the others are the
detailed contracts that this one orchestrates.

## Bootstrap order

The order matters: each step depends on the previous one. Take them as a
checklist; do not skip ahead.

### Step 1 — Project skeleton and Maven

1. Create the `server/` Maven module with packaging `jar`, parent
   `org.springframework.boot:spring-boot-starter-parent:4.1.0-M4`,
   `<java.version>25</java.version>`.
2. Add the repository declarations: Maven Central, the two pdg Nexus
   repositories, Vaadin Directory, Spring Milestones (and a pluginRepository
   for `vaadin-prereleases`). Reference values: see
   [`08-build-test-and-ops.md` § Repositories](08-build-test-and-ops.md#repositories).
3. Add the dependency set described in
   [`08-build-test-and-ops.md` § Stack of record](08-build-test-and-ops.md#stack-of-record).
   Pay attention to the `spring-boot-grpc-test` exclusion under
   `spring-boot-starter-test-classic`.
4. Configure plugins: `spring-boot-maven-plugin` (with `wait=500`,
   `maxAttempts=240`), `vaadin-maven-plugin` (`frontendHotdeploy=true`,
   `prepare-frontend` at `process-resources`), Surefire, Failsafe, JaCoCo
   (three executions: prepare-agent / prepare-agent-integration / report).
5. Define the four profiles: `dev`, `production`, `it`, `github`. The `it`
   profile is the load-bearing one — it starts and stops the Spring Boot
   app around Failsafe and injects the Mockito 2.23.4 javaagent via a
   `@{argLine}` Surefire `argLine`.
6. Set `<defaultGoal>spring-boot:run</defaultGoal>`.

### Step 2 — Application properties and Docker DBs

1. Author `src/main/resources/application.properties` per
   [`05-persistence-and-mappers.md` § Connection settings](05-persistence-and-mappers.md#connection-settings).
   Both Mongo and MySQL connection strings, Hibernate `ddl-auto=update`,
   Vaadin hot-deploy, devtools poll/quiet periods, the remember-me key
   placeholder, and the Ollama base URL.
2. Author `src/test/resources/application.properties` with embedded Mongo
   (`spring.mongodb.database=test`,
   `de.flapdoodle.mongodb.embedded.version=7.1.0`) and
   `junit-platform.properties` selecting `ClassOrderer$OrderAnnotation`.
3. Author the two `adventureDb/` Docker compose files:
   `Dockerfile.mongodb.yaml` (mongo 8.0.0 + mongo-express on 8081) and
   `Dockerfile.mysql.yaml` (mysql 9.6.0 + adminer on 8881).
4. Run `docker compose -f adventureDb/Dockerfile.mongodb.yaml up -d` and
   the equivalent for MySQL; verify connectivity.

### Step 3 — JPA security model and Spring Security

1. Implement the JPA entities in `security/model/` per
   [`03-domain-model.md` § Security entities](03-domain-model.md#security--access-control-entities)
   and [`05-persistence-and-mappers.md` § MySQL schema](05-persistence-and-mappers.md#mysql-schema):
   - `Role` enum (`ADMIN`, `AUTHOR`, `PLAYER`).
   - `UserData implements UserDetails` with ULID `@PrePersist`, unique
     `username`, `@ElementCollection(EAGER)` of roles, `isEnabled` flag.
   - `AdventureAuthor` with `adventureId` PK and `@ManyToOne UserData`.
   - `AdventurePlayer` with `@EmbeddedId AdventurePlayerId(adventureId, userId)`
     and `@MapsId @ManyToOne UserData`.
2. Add the JPA repositories in `server/security/repository/`:
   `UserRepository` (with `findByUsername`, `findByRolesContaining`),
   `AdventureAuthorRepository`, `AdventurePlayerRepository`.
3. Add the security services in `server/security/service/`:
   `CustomUserDetailsService`, `UserService`, `AdventureAccessService`.
   `AdventureAccessService` is the cross-store coordinator — see
   [`06-security-and-access-control.md` § AdventureAccessService](06-security-and-access-control.md#adventure-access-bridge-adventureaccessservice).
4. Implement `config/SecurityConfig.java` per
   [`06-security-and-access-control.md`](06-security-and-access-control.md#spring-security-wiring):
   `@EnableWebSecurity @EnableMethodSecurity`, role hierarchy bean,
   `BCryptPasswordEncoder`, `VaadinSecurityConfigurer.vaadin()`,
   `NullRequestCache`, single-session, remember-me, login form,
   URL guards, public allow-list.
5. Implement `config/DataInitializer.java` to seed the bootstrap admin
   when the table is empty. **Production rebuild:** read the password
   from a secret instead of hard-coding `admin123`.

### Step 4 — Database wiring

Implement `config/DatabaseConfig.java`:

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.pdg.adventure.server.security.repository")
@EnableMongoRepositories(basePackages = "com.pdg.adventure.server.storage.repository")
public class DatabaseConfig { }
```

Add `server/storage/mongo/MongoDbConfig` (`@EnableMongoAuditing`),
the `UuidIdGenerationMongoEventListener` (HIGHEST_PRECEDENCE), the
`@CascadeSave` / `@CascadeDelete` annotations, the
`CascadeSaveMongoEventListener`, and `CascadeDeleteHelper`. See
[`05-persistence-and-mappers.md` § Cascade save / delete](05-persistence-and-mappers.md#cascade-save).

### Step 5 — MongoDB documents and repositories

1. Implement `model/basic/` (`BasicData`, `DatedData`, `BasicDescriptionData`,
   `DescriptionData`, `CommandDescriptionData`).
2. Implement the documents in `model/`:
   `AdventureData`, `LocationData`, `ItemData`, `ItemContainerData`,
   `DirectionData`, `CommandData`, `CommandChainData`,
   `CommandProviderData`, `MessageData`, `VocabularyData`, `Word`, `ThingData`.
   Apply the cascade annotations exactly as documented.
3. Add the `*ActionData` and `*ConditionData` subclasses under
   `model/action/` and `model/condition/`.
4. Add MongoDB repositories under `server/storage/repository/`:
   `AdventureRepository`, `LocationRepository`, `ItemRepository`,
   `MessageRepository`, `WordRepository`, `VocabularyRepository`
   (rename — the current `VocabularyReporitory` typo SHOULD NOT be
   carried over).
5. Add the storage services: `AdventureService`, `ItemService`,
   `MessageService`. Wire `CascadeDeleteHelper` into `AdventureService.deleteAdventure`.

### Step 6 — Business objects

Implement runtime BOs in `server/`:

- `Adventure`, `AdventureConfig` (Spring `@Configuration` with the bean
  factories for `allLocations` / `allItems` / `allContainers` / `allWords` /
  `allMessages` / `allVariables`).
- `server/location/{Location, GenericDirection}`.
- `server/tangible/{Thing, Item, GenericContainer, ItemIdentifier}`.
- `server/vocabulary/Vocabulary` (wrapping `VocabularyData`).
- `server/storage/message/MessagesHolder`.
- `server/support/{Variable, VariableProvider, ArticleProvider, DescriptionProvider}`.
- The `api/` interfaces (`Action`, `PreCondition`, `Command`,
  `CommandChain`, `CommandDescription`, `Container`, `Containable`,
  `Wearable`, `Visitable`, `Actionable`, `HasCommands`, `HasLight`,
  `Describable`, `Ided`, `Mapper`, `ExecutionResult`).

### Step 7 — Mapper layer

1. Implement `api/Mapper<DO, BO>`.
2. Create `server/annotation/AutoRegisterMapper` (TYPE-targeted runtime
   annotation) and `AutoMapperRegistrationProcessor` (single
   `BeanPostProcessor`, `@Order(1001)`, queues registrations in
   `postProcessAfterInitialization`, registers them all in
   `postProcessBeforeInitialization` of the `MapperSupporter` bean).
3. Implement `server/support/MapperSupporter` — the registry that holds
   shared collections (vocabulary, mapped items / locations / containers,
   messages, variables) and the `Mapper` map.
4. Implement the mappers in `server/mapper/`:
   `AdventureMapper`, `VocabularyMapper`, `LocationMapper`,
   `DirectionMapper`, `ItemMapper`, `ItemContainerMapper`, `ThingMapper`,
   `CommandMapper`, `CommandChainMapper`, `CommandProviderMapper`,
   `CommandDescriptionMapper`, `DescriptionMapper`. Plus per-type mappers
   under `mapper/action/` and `mapper/condition/`.
5. Annotate every mapper with `@Component @AutoRegisterMapper(priority = 100, …)`.
6. **Finish the bits the current code marks as TODO:** `CommandMapper`
   (full DO ↔ BO including pre-conditions, action, follow-ups),
   destination resolution in `LocationMapper`, item-content mapping in
   `ItemContainerMapper`. See
   [`05-persistence-and-mappers.md` § Known gaps](05-persistence-and-mappers.md#known-gaps).

### Step 8 — Action / condition library

Implement every `Action` and `PreCondition` in `server/action/` and
`server/condition/` per
[`04-runtime-engine.md` § Action catalog](04-runtime-engine.md#action-catalog) and
[`04-runtime-engine.md` § PreCondition catalog](04-runtime-engine.md#precondition-catalog).

Pay attention to:

- The `AbstractAction` / `IdedAction` / `AbstractVariableAction` base
  classes.
- The `AbstractCondition` / `AbstractVariableCondition` base classes.
- The exact failure-message wording (used by tests and surfaced to the
  player).
- Engine messages: `MessagesHolder` reserves the negative-id slots
  (`-6`, `-8`, `-9`, `-10`, `-13`) for take/drop/wear feedback.

### Step 9 — Parser and engine

1. Implement `server/parser/`:
   `Parser`, `CommandHandler` (with `examineFallback` support),
   `CommandExecutor` (pocket → location → reduce-by-adjective → execute),
   `CommandMatcher`, `GenericCommand`, `GenericCommandDescription`,
   `GenericCommandProvider`, `GenericCommandChain`,
   `CommandExecutionResult`, `ExamineFallbackAction`.
2. Implement `server/engine/`:
   `GameContext`, `Workflow` (preCommands and interceptorCommands as
   `TreeMap<CommandDescription, Command>`), `GameLoop`,
   `ContainerSupplier`, `IO`. **Make `IO` injectable** — a
   `Consumer<String>` injected via `GameContext` is the recommended
   shape, so the engine is reusable from a Vaadin view.
3. Implement `CommandFactory.java` (top-level, in
   `com.pdg.adventure`): the take/drop/wear/look wiring exactly as
   described in
   [`04-runtime-engine.md` § CommandFactory](04-runtime-engine.md#commandfactory-wiring-conventions).
4. Implement `MiniAdventure.java` and `AdventureClient.java` for CLI play.
   These are useful for end-to-end testing even after the in-browser play
   surface lands.

### Step 10 — AI integration (intentional placeholder)

Implement `server/ai/OllamaConfig.java` to bind
`spring.ai.ollama.base-url` to a config property. Wire `DescribeAction`
to use it via dependency injection (drop the hardcoded URL in the
existing `fillThroughAI`). Default to **disabled** — make AI
augmentation a feature flag per Adventure or per Location, not an
inlined branch. See
[`04-runtime-engine.md` § Action catalog](04-runtime-engine.md#action-behavioural-detail)
and
[`01-product-overview.md` § In-scope but explicitly aspirational](01-product-overview.md#in-scope-but-explicitly-aspirational).

### Step 11 — Vaadin views

1. Implement `AdventureBuilderServer.java` with `@SpringBootApplication`
   and `@PWA(name = "Adventure Builder", ...)`. **Do not** ship with
   `@SpringBootApplication` commented out as the current code does.
2. Implement the layouts (`AdventureAppLayout`, the per-area
   `*MainLayout`s) per
   [`07-ui-and-navigation.md` § Layouts](07-ui-and-navigation.md#layouts).
3. Implement the reusable components:
   `BaseEditorView`, `ResetBackSaveView`, `VocabularyPicker`,
   `VocabularyPickerField`, `AdventureGrid`, `GridFactory`,
   `NavigationHelper`. And the support utilities: `ViewSupporter`,
   `RouteIds`, `GridProvider`, `TrackedUsage`.
4. Implement views in this order so each layer can lean on the previous:
   - `RootView`, `LoginView`, `LogoutView`, `AboutView`.
   - `AdminDashboardView`, `AuthorDashboardView`, `PlayerLibraryView`.
   - Adventure: `AdventuresMenuView`, `AdventureEditorView`.
   - Location: `LocationsMenuView`, `LocationEditorView`,
     `LocationMapView`.
   - Item: `ItemsMenuView`, `AllItemsMenuView`, `ItemEditorView`.
   - Direction: `DirectionsMenuView`, `DirectionEditorView`.
   - Command: `CommandsMenuView`, `CommandEditorView`,
     `view/command/action/*`.
   - Message: `MessagesMenuView`, `MessageEditorView`.
   - Vocabulary: `VocabularyMenuView`, `WordEditorDialogue`,
     `SpecialWordsView`.
   - Admin: `UserManagementView`, `AdventureAssignmentView`.
   - Player: a real **`*PlayView`** that drives `GameLoop` — this is
     missing from the current code and is the largest TODO.
5. Annotate every view with the right `@Route` and `@RolesAllowed` /
   `@AnonymousAllowed` per
   [`07-ui-and-navigation.md` § Route × role matrix](07-ui-and-navigation.md#route--role-matrix).

### Step 12 — Tests

1. Set up the test layout per
   [`08-build-test-and-ops.md` § Tests](08-build-test-and-ops.md#tests),
   mirroring `src/main/java`.
2. Author `TestSupporter` in `server/testhelper/` first; later tests
   should use it rather than ad-hoc fixture construction.
3. Add unit tests for every Action, Condition, mapper, service, parser
   class, and key view-supporter helper. AssertJ-only; no
   `assertEquals`.
4. Add browserless Vaadin tests for each `*EditorView`. Use
   `com.vaadin.browserless.BrowserlessTest`, scope `$()` queries to the
   view, and apply the ComboBox workarounds when interacting with
   pickers.
5. Add the JSON test fixtures: `OneLocation.json`, `QuickAdventure.json`,
   `Vocab.json`.
6. Add `MiniAdventureTest` / `AdventureBuilderTest` /
   `ApplicationTest` for end-to-end coverage with embedded MongoDB.
7. **Do not** introduce `BaseDTO.java` or `BaseRecord.java` PoC test
   classes — they were dead code in the previous repo and should not be
   carried over.

### Step 13 — CI

Add `.github/workflows/build.yml`:

- Trigger on push to `main` (and ideally `pull_request`).
- JDK 25 (`actions/setup-java@v4.5.0`, distribution `temurin`).
- Cache `~/.sonar/cache` and `~/.m2`.
- Run:
  ```
  mvn -B clean verify \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey=ABS \
    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
  ```
- Plumb the secrets `SONAR_TOKEN`, `SONAR_HOST_URL`.

Optionally add a separate workflow that runs `mvn dependency-check:check`
on a schedule and fails on HIGH/CRITICAL CVEs.

### Step 14 — Manual smoke

1. `mvn spring-boot:run`.
2. Browse to `http://localhost:8080`. Log in as `admin / admin123`.
3. Verify post-login redirect lands on `/admin/dashboard`.
4. Create an author, sign in as the author, create a tiny adventure,
   add a location, vocabulary, items, a direction, and a command.
5. Run the CLI: `java -cp target/server-*.jar com.pdg.adventure.MiniAdventure`
   (or use the `AdventureClient` wrapper) to play through it.

When all steps pass, you have parity with the current server module.

## Roadmap & known gaps (consolidated)

The chapters carry per-section gap lists. Here they are gathered into one
table with severity and pointer:

| Severity | Gap | Detail |
|----------|-----|--------|
| **Critical** | Hardcoded admin password | [`06-security-and-access-control.md`](06-security-and-access-control.md#known-gaps) — replace with secret-sourced bootstrap. |
| **Critical** | Hardcoded remember-me key | Same — override via env / secret. |
| **High** | `CommandMapper` incomplete | [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#known-gaps) — finish DO ↔ BO for commands, preconditions, actions, follow-ups. |
| **High** | `LocationMapper` destination resolution & `ItemContainerMapper` contents | Same. |
| **High** | `VocabularyReporitory` class-name typo | Same. |
| **High** | Missing player play surface (`*PlayView`) | [`02-functional-requirements.md` § C2](02-functional-requirements.md#c2-play-an-adventure-target-state), [`07-ui-and-navigation.md` § Known gaps](07-ui-and-navigation.md#known-gaps). |
| **High** | `AdventureBuilderServer` lacks `@SpringBootApplication` | [`01-product-overview.md` § Known gaps](01-product-overview.md#known-gaps). |
| **Medium** | NLP parser approach undecided | [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps). |
| **Medium** | Spring AI / Ollama integration commented out, base URL hardcoded | Same. |
| **Medium** | No per-game save state (variables not persisted) | [`03-domain-model.md` § Known gaps](03-domain-model.md#known-gaps). |
| **Medium** | `CreateAction` / `DestroyAction` skeletons | [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps). |
| **Medium** | `AmbiguousCommandException` declared but unused | Same. |
| **Low** | `NotConditionData` has no editor in `ConditionEditorFactory` | [`07-ui-and-navigation.md` § Known gaps](07-ui-and-navigation.md#known-gaps). |
| **Medium** | `UserService.delete` does not block on referencing rows | [`06-security-and-access-control.md` § Known gaps](06-security-and-access-control.md#known-gaps). |
| **Medium** | Cross-store atomicity gap on adventure create | [`05-persistence-and-mappers.md` § Cross-store consistency](05-persistence-and-mappers.md#cross-store-consistency). |
| **Medium** | No password change view | [`06-security-and-access-control.md` § Known gaps](06-security-and-access-control.md#known-gaps). |
| **Medium** | No account locking / failed-login throttling | Same. |
| **Medium** | No audit log | Same. |
| **Medium** | No HTTPS / HSTS enforcement | Same. |
| **Low** | `MessageData.translations / tags / category / notes` not surfaced in editor | [`03-domain-model.md` § Known gaps](03-domain-model.md#known-gaps). |
| **Low** | `ItemContainerData.holdingDirections` flag unused | Same. |
| **Low** | `CommandDescriptionData.setCommandSpecification` bypasses Vocabulary | Same. |
| **Low** | `AdventureService.preProcess`/`postProcess` empty | [`05-persistence-and-mappers.md` § Known gaps](05-persistence-and-mappers.md#known-gaps). |
| **Low** | `CommandsMenuView` uses `AdventuresMainLayout` instead of `CommandMainLayout` | [`07-ui-and-navigation.md` § Known gaps](07-ui-and-navigation.md#known-gaps). |
| **Low** | `VocabularyMenuView` carries a commented `@RouteAlias` | Same. |
| **Low** | Active-link drawer highlighting is JS-injected | Same. |
| **Low** | Mockito 2.23.4 javaagent quirk | [`08-build-test-and-ops.md` § Known gaps](08-build-test-and-ops.md#known-gaps). |
| **Low** | Milestone / pre-release stack pins | Same. |
| **Low** | No Flyway; schema by `ddl-auto=update` | [`05-persistence-and-mappers.md` § Known gaps](05-persistence-and-mappers.md#known-gaps). |
| **Low** | `BaseDTO.java` / `BaseRecord.java` PoC test classes | [`08-build-test-and-ops.md` § Known gaps](08-build-test-and-ops.md#known-gaps). |
| **Low** | `mvn_test.txt` untracked in repo | Same. |
| **Low** | `AGENTS-architecture.md` mentions two BeanPostProcessors at orders 1000 / 1001 — only one exists | [`05-persistence-and-mappers.md` § Mapper subsystem](05-persistence-and-mappers.md#mapper-subsystem). Update or retire that note. |

## Future module split

The current `server` module is a deliberate monolith. The intended split is:

| Future module | Will contain |
|---------------|--------------|
| `backend` | Domain, services, repositories, mappers, runtime engine, security. |
| `editor` | Vaadin views for authors and admins (every `*EditorView`, `*MenuView`, layouts under author/admin). |
| `player` | The play surface and any player-only support. |
| `api` | Shared interfaces and DTOs bridging the above. |

Guidance for the rebuild today, even before the split:

- Avoid coupling editor code to engine internals; route through services.
- Keep `view/` free of direct engine imports beyond what
  `BaseEditorView` already needs (mainly `AdventureService`).
- When placement is genuinely ambiguous, leave a
  `// TODO: Review placement for future module split` comment so the split
  PR can find it.

## Verification checklist

Before declaring the rebuild done, walk this list:

- [ ] `mvn clean verify` is green.
- [ ] JaCoCo report exists at `target/site/jacoco/index.html`.
- [ ] `mvn dependency-check:check` reports no HIGH or CRITICAL CVEs.
- [ ] `docker compose -f adventureDb/Dockerfile.mongodb.yaml up -d` and the
      MySQL equivalent both come up cleanly.
- [ ] `mvn spring-boot:run` exits cleanly with `Ctrl+C`; no stray
      thread / port leaks.
- [ ] Logging in as the seeded admin lands on `/admin/dashboard`.
- [ ] Creating an author, signing in as them, creating an adventure, a
      location, a vocabulary word, an item, a direction, and a command,
      then editing each one again, all work without errors and respect
      the BACK / SAVE / RESET / CANCEL contract.
- [ ] Running `MiniAdventure` against the saved adventure plays through.
- [ ] CI workflow on `main` is green.
- [ ] `find server/docs/specs -name '*.md'` lists this 10-document suite
      and every link inside resolves.

## Source pointers

This chapter draws on every other chapter; pointers per section live with
the relevant chapter. The high-leverage sources to keep open while you
rebuild are:

- `src/main/java/com/pdg/adventure/CommandFactory.java`
- `src/main/java/com/pdg/adventure/server/engine/{GameContext,GameLoop,Workflow}.java`
- `src/main/java/com/pdg/adventure/server/parser/{Parser,CommandHandler,CommandExecutor}.java`
- `src/main/java/com/pdg/adventure/server/support/MapperSupporter.java`
- `src/main/java/com/pdg/adventure/server/annotation/AutoMapperRegistrationProcessor.java`
- `src/main/java/com/pdg/adventure/server/security/service/AdventureAccessService.java`
- `src/main/java/com/pdg/adventure/config/{SecurityConfig,DatabaseConfig,DataInitializer}.java`
- `src/main/java/com/pdg/adventure/view/component/{AdventureAppLayout,BaseEditorView,ResetBackSaveView}.java`
- `pom.xml` and `src/main/resources/application.properties`.
