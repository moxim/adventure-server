# Workflow Editor View Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let an author attach `Command`s to an Adventure's Workflow through a new `WorkflowEditorView`, and prove those commands actually execute at runtime through `gameContext.preProcessCommands()` (the call `GameLoop.run()` makes each turn, `server/src/main/java/com/pdg/adventure/server/engine/GameLoop.java:36`).

**Architecture:** Add a `WorkflowData` model (a `List<CommandData>`) embedded directly in `AdventureData`, mirroring how `CommandProviderData`/`CommandChainData` are already embedded (no `@DBRef`, no separate Mongo collection). A new `WorkflowMapper` bridges persisted `WorkflowData` onto an already-constructed runtime `Workflow` (via its existing `addPreCommand`), reusing the existing `CommandMapper` for the actual `CommandData → Command` conversion. A new `WorkflowEditorView` (single Vaadin view, adventure-scoped) lets an author list, add, edit and delete these commands, reusing the existing `PreconditionActionEditor`/`VocabularyPickerField` components rather than duplicating `CommandEditorView`'s Location/Item-chain machinery. The existing (currently disabled) "Manage Workflow" button on `AdventureEditorView` is wired to it.

**Tech Stack:** Spring Boot, Vaadin Flow, MongoDB (Spring Data), Lombok, JUnit 5 + Mockito + AssertJ, `com.vaadin.browserless.BrowserlessTest` for UI-unit tests.

## Global Constraints

- All work happens in the `server` module (the actual git repo root is `server/`, not the project root).
- Follow `AGENTS-conventions.md` naming: business object (no suffix) → `*Data` → `*Mapper` → Vaadin `*View`/`*EditorView`/`*MenuView`/`*Layout`.
- `RolesAllowed("ROLE_AUTHOR")` on all new author-facing views, matching every existing adventure-scoped view.
- No new Mongo migration needed — MongoDB is schema-free; a new field on `AdventureData` just defaults via the constructor for pre-existing documents.

## Scope note (read before starting)

`GameContext`/`GameLoop`/`Workflow`/`MiniAdventure` are **demo/legacy-only** code today — confirmed by:
- `server/src/main/java/com/pdg/adventure/server/Adventure.java:66-70` — the real Vaadin web session's `run()` method has its entire `GameLoop` wiring commented out.
- `server/src/main/java/com/pdg/adventure/AdventureClient.java:21` — the only Spring Boot entry point that drives `MiniAdventure`/`GameLoop` has `@SpringBootApplication` commented out, so it isn't a live, runnable app today.
- `server/src/test/java/com/pdg/adventure/MiniAdventureTest.java` is `@Disabled`.

Because of this, this plan does **not** modify `MiniAdventure.java`/`LoadAdventureAction.java`/`AdventureClient.java` to make the demo CLI actually load authored workflow commands at boot — that would be non-trivial bootstrap surgery (the reload loop in `MiniAdventure.run()` unconditionally rebuilds the `Workflow` from scratch after every `ReloadAdventureException`, clobbering anything set up earlier) on code with zero live consumers, for zero observable behavior change. Instead, Task 1's test proves the exact mechanism the goal describes — `WorkflowMapper.populate()` feeding `Workflow.addPreCommand()`, executed via `gameContext.preProcessCommands()`, the identical call `GameLoop.run()` makes — and Task 1 leaves a `// TODO: Review needed` comment at the real seam (`MiniAdventure.run()`, right after `commandFactory.setUpWorkflowCommands(wf)`) for whoever eventually revives that demo entry point.

---

### Task 1: WorkflowData model, AdventureData wiring, and the WorkflowMapper engine bridge

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/model/WorkflowData.java`
- Modify: `server/src/main/java/com/pdg/adventure/model/AdventureData.java:44`
- Modify: `server/src/main/java/com/pdg/adventure/MiniAdventure.java` (TODO comment only)
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/WorkflowMapper.java`
- Test: `server/src/test/java/com/pdg/adventure/server/mapper/WorkflowMapperTest.java`

**Interfaces:**
- Consumes: `CommandMapper.mapToBO(CommandData): Command` (existing, `server/src/main/java/com/pdg/adventure/server/mapper/CommandMapper.java:30`); `Workflow.addPreCommand(GenericCommandDescription, Command): void` and `GameContext.setUpWorkflows(): Workflow` / `GameContext.preProcessCommands(): void` (existing, unchanged).
- Produces: `WorkflowData` (new model, `getCommands(): List<CommandData>`), `WorkflowMapper.populate(WorkflowData, Workflow): void` — later tasks call this type but this plan does not wire it into any runtime bootstrap (see Scope note above); it exists to be exercised directly by its own test and to be available as the authoring-to-runtime bridge if/when the demo entry point is revived.

- [ ] **Step 1: Create the `WorkflowData` model**

`server/src/main/java/com/pdg/adventure/model/WorkflowData.java`:

```java
package com.pdg.adventure.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorkflowData {
    private List<CommandData> commands = new ArrayList<>();
}
```

No `@DBRef`/`@CascadeSave`/`@CascadeDelete` — mirrors `CommandChainData`/`CommandProviderData`, which embed `CommandData` the same way with no Mongo cascade annotations.

- [ ] **Step 2: Embed `WorkflowData` in `AdventureData`**

In `server/src/main/java/com/pdg/adventure/model/AdventureData.java`, replace line 44:

```java
    // WorkflowData workFlow = new WorkflowData();
```

with:

```java
    private WorkflowData workflowData = new WorkflowData();
```

(Same package, no import needed. Lombok `@Data` on the class generates `getWorkflowData()`/`setWorkflowData()`.)

- [ ] **Step 3: Write the failing test for `WorkflowMapper`**

`server/src/test/java/com/pdg/adventure/server/mapper/WorkflowMapperTest.java`:

```java
package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;

@ExtendWith(MockitoExtension.class)
class WorkflowMapperTest {

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private Command command;

    private WorkflowMapper workflowMapper;

    @BeforeEach
    void setUp() {
        workflowMapper = new WorkflowMapper(commandMapper);
    }

    @Test
    void populate_addsMappedCommandsAsWorkflowPreCommands_soPreProcessCommandsExecutesThem() {
        // Given: an authored CommandData in a WorkflowData
        CommandData commandData = new CommandData(new CommandDescriptionData("shiver||"));
        WorkflowData workflowData = new WorkflowData();
        workflowData.getCommands().add(commandData);

        GenericCommandDescription runtimeDescription = new GenericCommandDescription("shiver", "", "");
        when(commandMapper.mapToBO(commandData)).thenReturn(command);
        when(command.getDescription()).thenReturn(runtimeDescription);
        when(command.execute()).thenReturn(
                new CommandExecutionResult(ExecutionResult.State.SUCCESS, "The room grows cold."));

        GameContext gameContext = new GameContext();
        Workflow workflow = gameContext.setUpWorkflows();

        // When: populating the runtime workflow from the authored data
        workflowMapper.populate(workflowData, workflow);

        // Then: gameContext.preProcessCommands() - the exact call GameLoop.run() makes each turn
        // at GameLoop.java:36 - now executes the authored command.
        gameContext.preProcessCommands();

        verify(commandMapper).mapToBO(commandData);
        verify(command).execute();
    }
}
```

- [ ] **Step 4: Run the test to verify it fails**

Run: `mvn test -Dtest=WorkflowMapperTest`
Expected: FAIL — compilation error, `WorkflowMapper` does not exist yet.

- [ ] **Step 5: Implement `WorkflowMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/WorkflowMapper.java`:

```java
package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.GenericCommandDescription;

@Service
public class WorkflowMapper {

    private final CommandMapper commandMapper;

    public WorkflowMapper(CommandMapper aCommandMapper) {
        commandMapper = aCommandMapper;
    }

    // Workflow can only be constructed with its owning GameContext (see Workflow(GameContext)),
    // so this isn't a symmetric Mapper<WorkflowData, Workflow>. It instead layers the author's
    // persisted commands onto an already-constructed runtime Workflow, e.g. right after
    // GameContext.setUpWorkflows().
    public void populate(WorkflowData aWorkflowData, Workflow aWorkflow) {
        for (CommandData commandData : aWorkflowData.getCommands()) {
            Command command = commandMapper.mapToBO(commandData);
            CommandDescription description = command.getDescription();
            aWorkflow.addPreCommand((GenericCommandDescription) description, command);
        }
    }
}
```

(The cast is safe: every `CommandDescription` reaching this point was produced by `CommandDescriptionMapper.mapToBO`, which always constructs a `GenericCommandDescription` — see `server/src/main/java/com/pdg/adventure/server/mapper/CommandDescriptionMapper.java:40`.)

- [ ] **Step 6: Run the test to verify it passes**

Run: `mvn test -Dtest=WorkflowMapperTest`
Expected: PASS

- [ ] **Step 7: Leave a TODO at the real (currently dead) runtime seam**

In `server/src/main/java/com/pdg/adventure/MiniAdventure.java`, in `run()`, immediately after the line `commandFactory.setUpWorkflowCommands(wf);` (currently around line 106), add:

```java
                // TODO: Review needed — layer authored WorkflowData onto `wf` here via WorkflowMapper
                //  once this demo entry point loads a specific AdventureData again (see LoadAdventureAction).
                //  Not wired today: MiniAdventure/GameContext/GameLoop have no live caller (AdventureClient's
                //  @SpringBootApplication is commented out, and Adventure.run()'s GameLoop wiring is commented
                //  out too), so there is nothing to regression-test against yet.
```

Do not change any executable code in `MiniAdventure.java` — comment only.

- [ ] **Step 8: Run the full test suite to confirm nothing else broke**

Run: `mvn test`
Expected: PASS (same pass count as before, plus the one new `WorkflowMapperTest`).

- [ ] **Step 9: Commit**

```bash
git add server/src/main/java/com/pdg/adventure/model/WorkflowData.java \
        server/src/main/java/com/pdg/adventure/model/AdventureData.java \
        server/src/main/java/com/pdg/adventure/MiniAdventure.java \
        server/src/main/java/com/pdg/adventure/server/mapper/WorkflowMapper.java \
        server/src/test/java/com/pdg/adventure/server/mapper/WorkflowMapperTest.java
git commit -m "feat: add WorkflowData model and WorkflowMapper engine bridge"
```

(Run this from the actual repo root, i.e. `server/` — adjust paths to be relative to your cwd if you are already inside `server/`.)

---

### Task 2: WorkflowMainLayout

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/view/workflow/WorkflowMainLayout.java`

**Interfaces:**
- Consumes: `com.pdg.adventure.view.component.AdventureAppLayout` (existing base class, same one `MessagesMainLayout` extends).
- Produces: `WorkflowMainLayout` class, used as the `layout` for `WorkflowEditorView`'s `@Route` in Task 3.

This is a plain scaffolding class (no business logic to unit-test), mirroring `server/src/main/java/com/pdg/adventure/view/message/MessagesMainLayout.java` exactly. Its own correctness is exercised indirectly by `WorkflowEditorViewRoutingTest` in Task 3 (the view must route/render at all).

- [ ] **Step 1: Create `WorkflowMainLayout`**

`server/src/main/java/com/pdg/adventure/view/workflow/WorkflowMainLayout.java`:

```java
package com.pdg.adventure.view.workflow;

import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

/**
 * Main layout for workflow management views.
 * Provides consistent navigation and header for workflow-related pages.
 */
public class WorkflowMainLayout extends AdventureAppLayout {

    public WorkflowMainLayout() {
        String appName = "Workflow";
        Image appImage = new Image("icons/to-do-list.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
```

(`icons/to-do-list.gif` already exists at `server/src/main/resources/META-INF/resources/icons/to-do-list.gif`.)

- [ ] **Step 2: Compile to verify it builds**

Run: `mvn compile`
Expected: SUCCESS (no test for this trivial class; Task 3's routing test exercises it by routing through it).

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/pdg/adventure/view/workflow/WorkflowMainLayout.java
git commit -m "feat: add WorkflowMainLayout"
```

---

### Task 3: WorkflowEditorView

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/view/workflow/WorkflowEditorView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/workflow/WorkflowEditorViewRoutingTest.java`

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventureOrForward(BeforeEnterEvent, AdventureAccessService): Optional<AdventureData>`; `AdventureData.getWorkflowData(): WorkflowData`; `AdventureService.saveAdventureData(AdventureData): void`; `PreconditionActionEditor(AdventureData)` / `.setCommand(CommandData)` / `.saveToCommand(CommandData)` / `.validate(): boolean` / `.setOnChange(Runnable)`; `VocabularyPickerField(String, String)` / `.populate(Collection<Word>)`; `CommandViewModel(CommandDescriptionData)`; `PreconditionActionFormatter(AdventureData)` / `.formatConditions(List<PreConditionData>): List<String>` / `.formatActions(List<ActionData>): List<String>`; `ViewSupporter.getWordText/formatDescription/getConfirmDialog/setSize`; `RouteIds.ADVENTURE_ID`.
- Produces: `WorkflowEditorView` routable at `author/adventures/:adventureId/workflow`, consumed by Task 4's `AdventureEditorView` button.

- [ ] **Step 1: Write the failing routing test**

`server/src/test/java/com/pdg/adventure/view/workflow/WorkflowEditorViewRoutingTest.java`:

```java
package com.pdg.adventure.view.workflow;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class WorkflowEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private WorkflowEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new WorkflowEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    private static AdventureData adventureWithOneWorkflowCommand() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        WorkflowData workflowData = new WorkflowData();
        workflowData.getCommands().add(new CommandData(new CommandDescriptionData("shiver||")));
        adventure.setWorkflowData(workflowData);
        return adventure;
    }

    @SuppressWarnings("unchecked")
    private static Grid<CommandData> grid(WorkflowEditorView view) {
        return (Grid<CommandData>) (Grid<?>) find(Grid.class, view).single();
    }

    @Test
    void beforeEnter_validAdventureId_populatesGridFromWorkflowData() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Workflow for The Demo");
        assertThat(test(grid(view)).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void beforeEnter_populatesVocabularyPickers_fromAdventureVocabulary() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createNewWord("shiver", com.pdg.adventure.model.Word.Type.VERB);
        adventure.setVocabularyData(vocabulary);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getVerbSelector().getListDataView().getItems().toList())
                .extracting(com.pdg.adventure.model.Word::getText)
                .contains("shiver");
    }

    @Test
    void newCommandButton_startsWithDeleteDisabledAndSaveDisabled() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        Button newCommandButton = find(Button.class, view).withText("New Command").single();
        Button deleteButton = find(Button.class, view).withText("Delete Command").single();
        Button saveButton = find(Button.class, view).withText("Save Command").single();

        assertThat(deleteButton.isEnabled()).isFalse();
        assertThat(saveButton.isEnabled()).isFalse();

        test(newCommandButton).click();

        assertThat(deleteButton.isEnabled()).isFalse();
        assertThat(saveButton.isEnabled()).isFalse();
    }

    @Test
    void selectingExistingCommand_enablesDeleteButton() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));
        CommandData existing = adventure.getWorkflowData().getCommands().getFirst();

        grid(view).select(existing);

        Button deleteButton = find(Button.class, view).withText("Delete Command").single();
        assertThat(deleteButton.isEnabled()).isTrue();
    }

    @Test
    void deletingSelectedCommand_removesFromWorkflowDataAndPersistsAdventure() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));
        CommandData existing = adventure.getWorkflowData().getCommands().getFirst();
        grid(view).select(existing);

        Button deleteButton = find(Button.class, view).withText("Delete Command").single();
        test(deleteButton).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        test(confirm).confirm();

        verify(adventureService).saveAdventureData(adventure);
        assertThat(adventure.getWorkflowData().getCommands()).isEmpty();
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn test -Dtest=WorkflowEditorViewRoutingTest`
Expected: FAIL — compilation error, `WorkflowEditorView` (and its `getVerbSelector()` test seam) do not exist yet.

- [ ] **Step 3: Implement `WorkflowEditorView`**

`server/src/main/java/com/pdg/adventure/view/workflow/WorkflowEditorView.java`:

```java
package com.pdg.adventure.view.workflow;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.CommandViewModel;
import com.pdg.adventure.view.command.PreconditionActionEditor;
import com.pdg.adventure.view.command.PreconditionActionFormatter;
import com.pdg.adventure.view.component.VocabularyPicker;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/workflow", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class WorkflowEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowEditorView.class);

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;

    private final Binder<CommandViewModel> binder;
    private final VocabularyPicker verbSelector;
    private final VocabularyPicker adjectiveSelector;
    private final VocabularyPicker nounSelector;
    private final Span preconditionAndActionHolder;
    private PreconditionActionEditor preconditionActionEditor;

    private final Grid<CommandData> grid;
    private final Button backButton;
    private final Button newCommandButton;
    private final Button deleteCommandButton;
    private final Button saveCommandButton;

    private AdventureData adventureData;
    private WorkflowData workflowData;
    private transient PreconditionActionFormatter formatter;
    private transient CommandViewModel cvm;
    private transient CommandData selectedCommand;
    private boolean editorHasChanges = false;
    private String pageTitle;

    public WorkflowEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(CommandViewModel.class);

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.");
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.");
        setUpBinding();

        Span helpText = new Span("Workflow commands run automatically every turn, in "
                + "gameContext.preProcessCommands() - they are not triggered by matching player input. "
                + "Verb/Adjective/Noun just label the command. Add preconditions to control when it fires; "
                + "an unmet precondition still shows its message every turn, it does not silently skip.");
        helpText.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");

        grid = buildGrid();
        grid.addSelectionListener(selection -> selection.getFirstSelectedItem().ifPresent(this::loadIntoEditor));

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        newCommandButton = new Button("New Command", _ -> loadIntoEditor(new CommandData()));

        deleteCommandButton = new Button("Delete Command", _ -> confirmDeleteCommand(selectedCommand));
        deleteCommandButton.setEnabled(false);

        saveCommandButton = new Button("Save Command", _ -> saveCommand());
        saveCommandButton.setEnabled(false);

        HorizontalLayout gridButtons = new HorizontalLayout(backButton, newCommandButton, deleteCommandButton);
        VerticalLayout gridSection = new VerticalLayout(gridButtons, grid);
        gridSection.setSizeFull();

        HorizontalLayout commandFieldsRow = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        preconditionAndActionHolder = new Span();
        VerticalLayout editorSection = new VerticalLayout(new NativeLabel("Preconditions & Actions"),
                                                          preconditionAndActionHolder, saveCommandButton);

        setSizeFull();
        setMargin(true);
        setPadding(true);
        add(helpText, gridSection, commandFieldsRow, editorSection);
    }

    private void setUpBinding() {
        binder.forField(verbSelector).asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(CommandViewModel::getVerb, CommandViewModel::setVerb);
        binder.forField(adjectiveSelector).bind(CommandViewModel::getAdjective, CommandViewModel::setAdjective);
        binder.forField(nounSelector).bind(CommandViewModel::getNoun, CommandViewModel::setNoun);

        binder.addStatusChangeListener(event -> updateSaveButtonState());
    }

    private Grid<CommandData> buildGrid() {
        Grid<CommandData> aGrid = new Grid<>(CommandData.class, false);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getVerb()))
             .setHeader("Verb").setAutoWidth(true);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getAdjective()))
             .setHeader("Adjective").setAutoWidth(true);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getNoun()))
             .setHeader("Noun").setAutoWidth(true);
        aGrid.addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatConditions(cmd.getPreConditions()))))
             .setHeader("Preconditions").setAutoWidth(true);
        aGrid.addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatActions(cmd.getActions()))))
             .setHeader("Actions").setAutoWidth(true);
        aGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        aGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        aGrid.setEmptyStateText("No workflow commands yet. Create one to get started.");
        ViewSupporter.setSize(aGrid);
        return aGrid;
    }

    /** Stack each rendered line in its own Span so multi-entry precondition/action cells wrap vertically. */
    private static Component stack(List<String> lines) {
        Div box = new Div();
        box.getStyle().set("display", "flex").set("flex-direction", "column");
        lines.forEach(line -> box.add(new Span(line)));
        return box;
    }

    private void loadIntoEditor(CommandData aCommandData) {
        selectedCommand = aCommandData;
        cvm = new CommandViewModel(aCommandData.getCommandDescription());
        binder.readBean(cvm);
        preconditionActionEditor.setCommand(aCommandData);
        editorHasChanges = false;
        deleteCommandButton.setEnabled(workflowData.getCommands().contains(aCommandData));
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        boolean valid = binder.isValid() && preconditionActionEditor != null && preconditionActionEditor.validate();
        saveCommandButton.setEnabled(valid && (binder.hasChanges() || editorHasChanges));
    }

    private void saveCommand() {
        try {
            if (!preconditionActionEditor.validate() || !binder.validate().isOk()) {
                return;
            }
            binder.writeBean(cvm);
            selectedCommand.setCommandDescription(cvm.getData());
            preconditionActionEditor.saveToCommand(selectedCommand);

            if (!workflowData.getCommands().contains(selectedCommand)) {
                workflowData.getCommands().add(selectedCommand);
            }

            adventureService.saveAdventureData(adventureData);
            refreshGrid();
            editorHasChanges = false;
            loadIntoEditor(new CommandData());
        } catch (ValidationException e) {
            LOG.error(e.getMessage());
        }
    }

    private void confirmDeleteCommand(CommandData aCommand) {
        var dialog = ViewSupporter.getConfirmDialog("Delete Command", "command",
                ViewSupporter.formatDescription(aCommand.getCommandDescription()));
        dialog.addConfirmListener(_ -> {
            workflowData.getCommands().remove(aCommand);
            adventureService.saveAdventureData(adventureData);
            refreshGrid();
            loadIntoEditor(new CommandData());
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(new ArrayList<>(workflowData.getCommands()));
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        pageTitle = "Workflow for " + resolvedAdventure.get().getTitle();
        populate(resolvedAdventure.get());
    }

    private void populate(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        workflowData = adventureData.getWorkflowData();
        formatter = new PreconditionActionFormatter(adventureData);

        if (preconditionActionEditor == null) {
            preconditionActionEditor = new PreconditionActionEditor(adventureData);
            preconditionActionEditor.setOnChange(() -> {
                editorHasChanges = true;
                updateSaveButtonState();
            });
            preconditionAndActionHolder.add(preconditionActionEditor);
        }

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        verbSelector.populate(vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        adjectiveSelector.populate(vocabularyData.getWords(ADJECTIVE).stream().filter(word -> word.getSynonym() == null).toList());
        nounSelector.populate(vocabularyData.getWords(NOUN).stream().filter(word -> word.getSynonym() == null).toList());

        refreshGrid();
        loadIntoEditor(new CommandData());
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || editorHasChanges);
    }

    /** Test seam: exposes the verb picker so tests can assert vocabulary reached it without relying on
     * ComboBox setValue()/getValue(), which is unreliable under BrowserlessTest. */
    VocabularyPicker getVerbSelector() {
        return verbSelector;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn test -Dtest=WorkflowEditorViewRoutingTest`
Expected: PASS. If a specific assertion fails (e.g. exact button label text, or `ConfirmDialog` interaction API), adjust the test or the view to match — the intent (grid reflects `workflowData.getCommands()`; selecting an existing command enables Delete; confirming Delete removes it and persists) is what matters, not incidental wording.

- [ ] **Step 5: Run the full suite**

Run: `mvn test`
Expected: PASS, same or higher pass count.

- [ ] **Step 6: Commit**

```bash
git add server/src/main/java/com/pdg/adventure/view/workflow/WorkflowEditorView.java \
        server/src/test/java/com/pdg/adventure/view/workflow/WorkflowEditorViewRoutingTest.java
git commit -m "feat: add WorkflowEditorView for authoring workflow commands"
```

---

### Task 4: Wire the "Manage Workflow" button on AdventureEditorView

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java:88-89`

**Interfaces:**
- Consumes: `WorkflowEditorView` (Task 3), `RouteIds.ADVENTURE_ID` (existing).
- Produces: nothing new — closes the loop from the adventure's main editor to the new view.

- [ ] **Step 1: Enable and wire the existing disabled button**

In `server/src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java`, replace lines 88-89:

```java
        Button workflowButton = new Button("Manage Workflow");
        workflowButton.setEnabled(false);
```

with:

```java
        Button workflowButton = new Button("Manage Workflow", _ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(WorkflowEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });
```

(This mirrors `editLocationsButton`/`editVocabularyButton`/`editMessagesButton`/`editItemsButton` immediately above it exactly.)

Add the import, alongside the other `view.*` imports:

```java
import com.pdg.adventure.view.workflow.WorkflowEditorView;
```

- [ ] **Step 2: Run the existing AdventureEditorView tests to confirm nothing broke**

Run: `mvn test -Dtest=AdventureEditorViewRoutingTest`
Expected: PASS (this file doesn't assert on `workflowButton`'s enabled state today, so enabling it doesn't break an existing assertion).

- [ ] **Step 3: Run the full suite**

Run: `mvn test`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add server/src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java
git commit -m "feat: wire Manage Workflow button to WorkflowEditorView"
```

---

## After implementation: manual verification

Run the app (`/run` skill, or your usual dev-server flow), open an adventure's editor, click "Manage Workflow", add a command with a `MessageActionData`-style action, save, and confirm it appears in the grid and survives a page reload (i.e. round-trips through Mongo via `AdventureService.saveAdventureData`/`findAdventureById`). This is the part `WorkflowEditorViewRoutingTest` cannot fully exercise (ComboBox interaction is unreliable under `BrowserlessTest` — see `WorkflowEditorViewRoutingTest`'s vocabulary-population test, which asserts the picker's item list rather than driving a full "pick a verb and save" flow through the UI).
