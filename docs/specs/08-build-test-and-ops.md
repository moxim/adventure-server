# 08 — Build, Test & Operations

## Purpose

This chapter is the operational handbook: the technology stack of record,
the Maven profiles, the local-development databases, the test strategy
(unit / browserless view / "integration"), the CI pipeline, and the
day-to-day commands. A rebuild that follows these settings will end up
binary-equivalent to the current server module.

## Stack of record

| Layer | Choice | Pinned in |
|-------|--------|-----------|
| Language | **Java 25** (`maven.compiler.source/target/release`) | `pom.xml` lines 16-18, 358 |
| Framework parent | **Spring Boot 4.1.0-M4** (milestone) | `pom.xml:29` |
| Web stack | Spring MVC + Vaadin 25.1.1 (prerelease) | `pom.xml:20` |
| Backend data | Spring Data MongoDB (Mongo 8.0) | `application.properties` |
| User data | Spring Data JPA (MySQL 9.6) | `application.properties` |
| Security | Spring Security + Vaadin `VaadinSecurityConfigurer` | `SecurityConfig.java` |
| Validation | `spring-boot-starter-validation` | `pom.xml:222` |
| AI | Spring AI 2.0.0-M1 BOM + `spring-ai-starter-model-ollama` | `pom.xml:307-322` |
| ID generation | `com.github.f4b6a3:ulid-creator:5.2.4` | `pom.xml:208-211` |
| Annotations | Lombok 1.18.42 | `pom.xml:354` |
| Tracing | OpenTelemetry instrumentation annotations 2.14.0 | `pom.xml:295-298` |
| Tests | JUnit Jupiter (via Spring Boot starter), Mockito 2.23.4 javaagent, AssertJ 4.0.0-M1 | `pom.xml:21-22, 285-286` |
| Embedded Mongo | `de.flapdoodle.embed.mongo.spring4x` 4.22.0 | `pom.xml:268-272` |
| Browserless Vaadin tests | `com.vaadin:browserless-test-junit6` | `pom.xml:290-293` |
| Refactoring | OpenRewrite `rewrite-spring` 6.24.0 | `pom.xml:323-327` |

The combination of milestones (Spring Boot 4.1.0-M4, Spring AI 2.0.0-M1,
AssertJ 4.0.0-M1) and prereleases (Vaadin 25) is intentional — the project
exercises cutting-edge releases. A rebuild MUST keep this combination or
explicitly justify a downgrade; mixing in stable Spring Boot 3.x will
break Spring AI integration.

## Repositories

`pom.xml` lists the artifact repositories (in priority order):

1. **Maven Central** — explicit declaration so it is unambiguously highest
   priority (snapshots disabled).
2. `pdg-internal` — `https://nexus.pdg-software.com/repository/pdg-internal/`
   (snapshots enabled).
3. `pdg-public` — `https://nexus.pdg-software.com/repository/pdg-public-group/`
   (releases only).
4. **Vaadin Directory** — `https://maven.vaadin.com/vaadin-addons` for
   add-ons such as `org.github.legioth:imagemap` and
   `org.parttio:canvas-java`.
5. **Spring Milestones** — `https://repo.spring.io/milestone` for
   Spring Boot 4.1.0-M4 and Spring AI 2.0.0-M1.

Plugin repositories add `vaadin-prereleases` for the 25.x plugin.

`pom.xml` also declares a `distributionManagement` pointing at the same
`pdg-internal` Nexus.

## Maven profiles

| Profile | Purpose | Effect |
|---------|---------|--------|
| _(default)_ | Run locally with hot-deploy | Default goal `spring-boot:run`. Vaadin frontend is hot-deployed (`<frontendHotdeploy>true</frontendHotdeploy>`). |
| `dev` | Run with JDWP debug | JVM args `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5195`. Attach IDE on port `5195`. |
| `production` | Build for production | Adds `vaadin-maven-plugin:build-frontend` (AOT) at `compile` phase with `productionMode=true`. Run as `mvn clean package -Pproduction`. |
| `it` | Integration tests | Spring Boot maven plugin starts the application before integration tests and stops it after. Failsafe runs `*IT.java`. Surefire injects `-javaagent:${org.mockito.mockito-core}` so Mockito 2.23.4 can mock final classes on Java 17+. Resolves dependency paths via `maven-dependency-plugin`. |
| `github` | CI (alternative repo set) | Adds GitHub Maven Packages (`https://maven.pkg.github.com/mafw/adventure-server`) to the repository list. |

Default goal is `spring-boot:run`. Spring Boot maven plugin is configured
with `<wait>500</wait><maxAttempts>240</maxAttempts>` so first-time Vaadin
startup (which can exceed the default 30 s) does not time out.

## JaCoCo

`org.jacoco:jacoco-maven-plugin:0.8.14` runs three executions:

| Execution | Phase | Goal |
|-----------|-------|------|
| `prepare-agent` | (default) | `prepare-agent` — instruments unit tests via Surefire. |
| `prepare-agent-integration` | `pre-integration-test` | `prepare-agent-integration` — instruments integration tests via Failsafe. |
| `report` | `verify` | `report` — emits XML and HTML at `target/site/jacoco/`. |

CI uploads `target/site/jacoco/jacoco.xml` to SonarQube.

## OWASP dependency check

`org.owasp:dependency-check-maven:12.1.8` is configured in
`<pluginManagement>` so any module can opt in by listing the plugin in its
`<plugins>` section. Run on demand:

```
mvn dependency-check:check
```

This is not bound to a Maven phase; CI runs it explicitly when needed.

## Spring Boot peculiarities

1. **`spring-boot-starter-classic` (test scope) and `spring-boot-starter-test-classic`**
   are used because Spring Boot 4 introduced split starters. The classic
   variant is the closest to Spring Boot 3.x defaults and is what the test
   suite expects.
2. **`spring-boot-grpc-test` is excluded** from
   `spring-boot-starter-test-classic` (`pom.xml:144-153`). It registers a
   `GrpcPortInfoApplicationContextInitializer$Listener` unconditionally via
   `spring.factories`, but the gRPC server core class it depends on is not
   on the classpath — every test publishing events would otherwise fail with
   `TypeNotPresentException`.
3. **`spring-boot-starter-properties-migrator`** is on the runtime classpath
   to surface deprecated property warnings during the Spring Boot 4 upgrade.

## Local development databases

`server/adventureDb/` holds Docker Compose definitions for the two
databases plus their web admin UIs.

### MongoDB (`Dockerfile.mongodb.yaml`)

| Service | Image | Ports | Credentials |
|---------|-------|-------|-------------|
| `mongo` | `mongo:8.0.0` | `27017:27017` | root: `root` / `example` (env `MONGO_INITDB_ROOT_USERNAME` / `..._PASSWORD`) |
| `mongo-express` | `mongo-express:1.0.2-20-alpine3.19` | `8081:8081` | basic-auth `admin` / `pass`; admin UI enabled |

Volume `mongodb_data` persists data across container restarts.

The application connects as `advAdmin` / `example`, authenticated against
`admin`, against database `adventures`. The `advAdmin` user must be created
manually after first startup; the SCRAM credentials are recorded as a
comment in `application.properties`.

### MySQL (`Dockerfile.mysql.yaml`)

| Service | Image | Ports | Credentials |
|---------|-------|-------|-------------|
| `mysql-db` | `mysql:9.6.0` | `3306:3306` | root: `root_secret_password`; app: `adventure_user` / `adventure_password`; database: `adventure_db` |
| `adminer` | `adminer:latest` | `8881:8080` | (no creds; uses MySQL creds) |

Volume `mysql_data` persists across restarts.

### Bring-up

```
docker compose -f adventureDb/Dockerfile.mongodb.yaml up -d
docker compose -f adventureDb/Dockerfile.mysql.yaml up -d
mvn spring-boot:run                  # default profile
# or:
mvn spring-boot:run -Pdev            # debug on 5195
```

Embedded MongoDB (`de.flapdoodle.embed.mongo.spring4x`) is the test-time
database — no Docker needed for `mvn test`.

## Tests

Layout is mirrored from `src/main/java`:

```
src/test/java/com/pdg/adventure/
├── MiniAdventureTest.java           ← end-to-end (full Spring context)
├── model/                           ← DO unit tests
│   ├── basic/
│   │   └── CommandDescriptionDataTest.java
│   ├── WordTest.java
│   ├── MessageDataTest.java
│   ├── GenericCommandProviderDataTest.java
│   └── VocabularyDataTest.java      ← tests for VocabularyData.findWordsBySynonym
├── server/
│   ├── action/                      ← Action unit tests
│   ├── condition/
│   ├── location/
│   ├── mapper/                       ← + mapper/action, mapper/condition
│   ├── parser/
│   ├── storage/
│   ├── support/
│   ├── tangible/
│   ├── vocabulary/
│   ├── testhelper/                   ← TestSupporter
│   └── AdventureBuilderTest, ApplicationTest
└── view/                              ← browserless Vaadin tests
    ├── command/, direction/, item/, location/, message/, vocabulary/
```

Test resources live in `src/test/resources/`:

| File | Purpose |
|------|---------|
| `application.properties` | Test config: `spring.mongodb.database=test`; `de.flapdoodle.mongodb.embedded.version=7.1.0`. |
| `junit-platform.properties` | `junit.jupiter.testclass.order.default = ClassOrderer$OrderAnnotation` — tests with `@Order` are honoured. |
| `OneLocation.json`, `QuickAdventure.json`, `Vocab.json` | JSON fixtures. |
| `EverlastAI-Test.http` | IntelliJ HTTP-client requests; not run by Maven. |

### Unit tests

- Convention: `*Test.java`, JUnit Jupiter + Mockito (`@ExtendWith(MockitoExtension.class)` or plain `mock()`) + AssertJ.
- AssertJ is the **only** assertion library — `assertThat(...)`. JUnit
  `assertEquals` is forbidden.
- Mock dependencies with `@Mock`; instantiate the class under test with
  `@InjectMocks`.
- Services return `Optional<T>` — every test SHOULD cover both present and
  empty cases.
- Place tests in the **mirrored package** of the class under test.

### TestSupporter (canonical fixture builder)

`server/testhelper/TestSupporter.java` is a static-only helper. It exposes
small, named factories that return ready-to-assert results:

| Helper | Purpose |
|--------|---------|
| `conditionToBoolean(PreCondition)` | Run `check()` and reduce to boolean — used in fluent assertions. |
| `addItemToBoolean(Container, Containable)` / `removeItemToBoolean(...)` | Run container ops and reduce the result to boolean. |
| `applyCommandToBoolean(Containable, GenericCommandDescription)` | Apply a command and reduce. |
| `createCommand(String id, VocabularyData)` | Create a `CommandData` with id-derived verb / adjective / noun (registers the words on the vocabulary in the process). |
| `createCommandDescriptionData(String id, VocabularyData)` | Same, returning just the description. |

Rules of engagement:

- **Do not** create ad-hoc fixtures inline if `TestSupporter` already
  provides what you need.
- **Add new factories** here rather than littering tests with builders.

### Browserless Vaadin tests

The view subtree (`src/test/java/.../view/`) uses `browserless-test-junit6`
to construct routed views without a browser or full Spring context.

```java
class SpecialWordsViewTest extends BrowserlessTest {

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);    // plain Mockito
        view = new SpecialWordsView(adventureService);
        UI.getCurrent().add(view);                          // attach to mock UI
    }
}
```

Use `com.vaadin.browserless.BrowserlessTest`, **not**
`com.vaadin.testbench.unit.BrowserlessTest`.

`SpringBrowserlessTest` is **not** appropriate for this project:
`ViewSupporter.getCurrentUser()` throws without a real Spring Security
context, and the test `application.properties` configures only MongoDB —
a full Spring context would fail on missing MySQL / JPA config.

#### Component queries

```java
$(VocabularyPickerField.class, view).all()        // scope to view, not router
$(VocabularyPickerField.class, view).atIndex(3)
$(Button.class, view).withText("Save").single()
```

Always pass the view as the second argument; otherwise the query searches
the router view.

#### Tester API

```java
test(button).click();
test(comboBox).selectItem("examine");   // see limitations below
```

#### Known limitations: ComboBox in browserless

`ComboBox` (and subclasses) rely on a `DataCommunicator` /
`DataKeyMapper` — neither is hooked up in browserless mode. Therefore:

| Operation | Works? | Notes |
|-----------|--------|-------|
| `comboBox.setValue(item)` | ❌ | Fails silently — `getValue()` still returns `null`. |
| `comboBox.getValue()` (null check) | ✅ | The default `null` is returned correctly. |
| `test(comboBox).selectItem("text")` | ❌ | Item lookup fails. |
| `test(button).click()` | ✅ | Button events work. |
| `$(ComboBox.class, view).all()` | ✅ | Component discovery works. |

**Workaround — preselection.** Assert against the model object
(`vocabularyData.getExamineWord()`) rather than the picker.

**Workaround — interaction.** When you must fire a value-change listener:

1. Grab the **real component** via reflection (the `$(...)` wrapper does
   not own the listeners):
   ```java
   Field f = SpecialWordsView.class.getDeclaredField("examineSelector");
   f.setAccessible(true);
   VocabularyPickerField picker = (VocabularyPickerField) f.get(view);
   ```
2. Set `AbstractField.fieldSupport.bufferedValue` directly so `getValue()`
   returns the chosen item.
3. Fire through `Component.eventBus` with the exact registered event class
   (`AbstractField.ComponentValueChangeEvent`).

The full snippet lives in the existing test files; reuse it rather than
re-deriving it.

### Integration tests

There are no `*IT.java` files today. Three "integration"-style tests run
inside the regular Surefire pass with full Spring context:

- `MiniAdventureTest` — drives the engine end-to-end against an in-memory
  embedded MongoDB.
- `AdventureBuilderTest` — exercises the builder flows.
- `ApplicationTest` — Spring Boot startup smoke test.

If the project later introduces true `*IT.java` tests, the `it` profile
(see above) is the right harness: it starts and stops the application
around Failsafe.

## Commands cheat-sheet

```
# Run with hot-deploy
mvn spring-boot:run

# Run with JDWP debug on 5195
mvn spring-boot:run -Pdev

# Production package (Vaadin AOT frontend)
mvn clean package -Pproduction

# All unit tests (uses embedded MongoDB)
mvn test

# Single test class
mvn test -Dtest=SpecialWordsViewTest

# Single test method
mvn test -Dtest=LocationServiceTest#shouldReturnEmptyWhenNotFound

# Integration tests (boots Spring Boot, runs Failsafe *IT.java)
mvn verify -Pit

# Full build + JaCoCo report at target/site/jacoco/
mvn verify

# Dependency vulnerability scan
mvn dependency-check:check

# CI-style (with Sonar token in env)
mvn -B clean verify \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey=ABS \
    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

## CI: GitHub Actions

`server/.github/workflows/build.yml`:

- Trigger: push to `main`.
- Runner: `ubuntu-latest`.
- JDK: 25 (`actions/setup-java@v4.5.0`, distribution `temurin`).
- Caches: `~/.sonar/cache`, `~/.m2`.
- Build step:
  ```
  mvn -B clean verify \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey=ABS \
    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
  ```
- Required secrets: `GITHUB_TOKEN`, `SONAR_TOKEN`, `SONAR_HOST_URL`.

## Frontend management

Vaadin Flow drives the frontend; the Java code is the only authoritative
surface (no separate React project under `src/`). Build-time orchestration:

- `vaadin-maven-plugin:prepare-frontend` runs at `process-resources`.
- `vaadin-maven-plugin:build-frontend` runs at `compile` under
  `-Pproduction`.
- `vite.config.ts` delegates to the Vaadin-generated `vite.generated.ts`.

Hot-deploy is on by default (`<frontendHotdeploy>true</frontendHotdeploy>`):
TypeScript / CSS edits trigger reload without restarting Spring Boot. The
`spring-boot-devtools` dependency restarts Spring on classpath changes; its
poll interval is `3000ms` and quiet period `1000ms`
(`application.properties`).

## Static assets

Bundled under `src/main/resources/META-INF/resources/`:

- `images/` — `adventure.png` (logo), `main.jpg`, `islandMap.jpg`,
  `icons8-location.gif`.
- `icons/` — area-themed gifs / pngs for layout drawers.
- `offline.html` — PWA offline fallback referenced by `@PWA(offlinePath)`.
- `banner.txt` — Spring Boot startup banner.

## Source pointers

- `pom.xml` — every choice above.
- `src/main/resources/application.properties`
- `src/main/resources/banner.txt`
- `src/main/resources/META-INF/resources/`
- `adventureDb/Dockerfile.mongodb.yaml`, `adventureDb/Dockerfile.mysql.yaml`
- `.github/workflows/build.yml`
- `src/test/resources/application.properties`,
  `src/test/resources/junit-platform.properties`
- `src/test/java/com/pdg/adventure/server/testhelper/TestSupporter.java`

## Known gaps

- **Milestone / pre-release stack.** Spring Boot 4.1.0-M4, Spring AI 2.0.0-M1,
  AssertJ 4.0.0-M1, Vaadin 25 prerelease — track GA releases and pin once
  available. The Spring AI testcontainers dependency is currently
  commented out because it does not work with this combination.
- **Mockito 2.23.4 is *very* old** (current line is 5.x). Upgrading
  removes the `-javaagent` workaround in the `it` profile, but requires
  re-checking final-class mocks across the test suite.
- **Old assertion / fixture libs** (AssertJ M1) may need version bumps when
  GA ships.
- **No `*IT.java` tests yet.** Add Failsafe-named tests when functionality
  warrants — the `it` profile is already wired to host them.
- **No Flyway.** `spring.jpa.hibernate.ddl-auto=update` is the schema
  source. Adding Flyway is on the roadmap (see
  [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#known-gaps)).
- **`mvn_test.txt`** appears as an untracked file in repo runs; ignore in
  `.gitignore` if it is local-only output.
- **CI runs only on push to `main`.** No PR-trigger; consider adding
  `pull_request` to surface failures earlier.
- **OWASP not in CI.** `dependency-check:check` is on demand; add it as a
  scheduled CI run or as part of the pre-merge pipeline.
- **Two PoC test classes to delete** when encountered:
  `server/BaseDTO.java` and `server/BaseRecord.java` in `src/test/java/.../server/`
  (per `AGENTS-testing.md`). They should not be carried into a rebuild.
