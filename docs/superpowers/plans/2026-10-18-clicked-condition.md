# CLICKED Condition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An author can add a `CLICKED(x, y)` precondition to a Process; clicking a cell of the 10 × 10 grid laid over the picture in the run view runs the Processes table, and a matching Process performs its actions.

**Architecture:** The run view turns a click on the displayed picture into a grid cell (pure `PictureGrid` helper) and calls `AdventureRunSession.submitClick`, which runs `GameLoop.processClick`. That records the click in `GameContext`, runs `Workflow.runProcesses()` and decides hit or miss through an "applied" hook that `GenericCommand` fires on its preconditions once they all passed. CLICKED follows the layering of `ChanceCondition`/`PrepositionCondition` (data, runtime condition, mapper, editor, selector, formatter).

**Tech Stack:** Java 25, Spring Boot, Vaadin Flow 25.2.6 (browserless tests), JUnit 5, AssertJ, Mockito, Lombok. Maven from `server/`.

**Spec:** `docs/superpowers/specs/2026-10-18-clicked-condition-design.md` (approved). Paths below are relative to `server/`; Java sources are under `src/main/java/com/pdg/adventure/` (written `main/…`) and `src/test/java/com/pdg/adventure/` (written `test/…`).

## Global Constraints

- Only the `server` module; the separate `api` and `editor` modules stay untouched (`com.pdg.adventure.api` is just a package inside `server`).
- Grid: **10 × 10**, cells numbered from **0**, **x = column (left to right), y = row (top to bottom)**, valid values **0–9**; the grid covers the picture itself, not letterbox margins; same numbering as `LocationMapView`.
- A click runs the Processes table only (no verb parsing, no location/item commands, no Responses, no Arrival Processes).
- Miss: system message **SM6** first, then the usual prompt (SM2–SM5, steered by PROMPT). Hit: the Processes' messages, then the prompt.
- A hit = some command whose preconditions include a positive `ClickedCondition` (not inside NOT) had all preconditions pass (its actions then ran, whether they succeeded or not). `NOT_CLICKED` never makes a hit.
- Typed fields in `GameContext` (verb, adjective, noun, preposition, adverb, noun2, adjective2) are empty during a click run; the click is cleared in a `finally`.
- A hit counts as a turn (TURNS +1), a miss does not.
- Selector entry text: `Clicked (matches a click on the picture)`, offered in the Processes editor only. Editor field labels: `X (column, 0–9)` and `Y (row, 0–9)`, both required. Formatter output: `CLICKED 3, 4`, `NOT_CLICKED 3, 4`.
- Add `// TODO: Review needed — [reason]` where this plan marks an ambiguity.
- The author commits; **do not run `git commit`**. Each task ends by leaving its files uncommitted.
- Run Maven from `server/` with `JAVA_HOME=/opt/homebrew/opt/openjdk@25`. A background mvn-compile hook can race with `mvn test` and print fake mass compile errors; rerun when only that appears.
- Tests follow `AGENTS-testing.md`; the changelog follows `AGENTS-update-changelog.md`.

## Review Focus

- A click on the empty margin of a letterboxed picture, or past the picture's edge (offset == width), is ignored and does not reach the engine — `PictureGridTest`, `AdventureRunViewTest` (Task 5).
- A typed turn after a click never sees CLICKED true, and a click never sees leftovers of the previous typed command (e.g. a Process gated on `PREPOSITION =? on`) — `ClickGameLoopTest` (Task 3).
- A click on a finished or superseded game does not run the Processes — `AdventureRunSessionTest` (Task 4).
- A Process that quits the game during a click ends the session and still clears the click state — `ClickGameLoopTest` (Task 3).
- CLICKED data with a missing or out-of-range x/y (half-filled or imported data) never matches and never throws — `ClickedConditionTest` (Task 1).

---

### Task 1: Click state, `ClickedCondition`, data class and mapper

**Files:**
- Create: `main/model/condition/ClickedConditionData.java`, `main/server/condition/ClickedCondition.java`, `main/server/mapper/condition/ClickedConditionMapper.java`
- Modify: `main/server/engine/GameContext.java`
- Test: `test/server/condition/ClickedConditionTest.java`, `test/server/mapper/condition/ClickedConditionMapperTest.java`, `test/server/engine/GameContextClickTest.java`

**Interfaces:**
- Produces (`GameContext`):
  - `void beginClick(int aColumn, int aRow)` — records the click, empties the seven typed fields, resets the handled flag
  - `void endClick()` — clears click and handled flag
  - `boolean isClickAt(Integer aColumn, Integer aRow)` — false when no click is active or either argument is null
  - `void markClickHandled()` / `boolean isClickHandled()`
- Produces: `ClickedConditionData extends PreConditionData` with `Integer x`, `Integer y` (Lombok `@Data`, same annotations as `ChanceConditionData`)
- Produces: `ClickedCondition extends AbstractCondition`, constructor `ClickedCondition(Integer aX, Integer aY, GameContext aGameContext)`, getters `Integer getX()`, `Integer getY()`; `check()` returns SUCCESS iff `gameContext.isClickAt(x, y)`, otherwise FAILURE with an empty message (silent, like `PrepositionCondition`)
- Produces: `ClickedConditionMapper` registered with `@AutoRegisterMapper(priority = 20, description = "ClickedCondition mapping")`, taking `(MapperSupporter, GameContext)` like `PrepositionConditionMapper`

- [ ] **Step 1: Write the failing tests**
  - `GameContextClickTest`: `isClickAt_isFalseWithoutAClick`; `beginClick_makesIsClickAtTrueOnlyForThatCell` (cell 3,4 true; 4,3 false; null,4 false); `beginClick_emptiesTheTypedFieldsThatWereSet` (set verb "take", noun "lamp", preposition "on", adverb, noun2, adjective, adjective2 first; all `VocabularyData.EMPTY_STRING` afterwards); `endClick_clearsTheClickAndTheHandledFlag`; `beginClick_resetsTheHandledFlag`.
  - `ClickedConditionTest` (mock `GameContext` like `PrepositionConditionTest`): check SUCCESS when `isClickAt(3, 4)` is true; FAILURE with empty message otherwise; `new ClickedCondition(null, 4, ctx).check()` and `(12, 3)` with a real `GameContext` that has `beginClick(3, 4)` → FAILURE, no exception; getters return x and y.
  - `ClickedConditionMapperTest` modelled on `ChanceConditionMapperTest`: data → BO → data round-trip keeps id, x, y.

- [ ] **Step 2: Run them, expect compile failures** — `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn test -Dtest='GameContextClickTest,ClickedConditionTest,ClickedConditionMapperTest'`

- [ ] **Step 3: Implement** the signatures above. In `GameContext` keep the click as two nullable `Integer` fields plus a `boolean clickHandled`; `beginClick` reuses the existing `setCurrent…(EMPTY_STRING)` setters. Class Javadoc on `ClickedCondition` says why the check reads `GameContext` (same reason as `PrepositionCondition`).

- [ ] **Step 4: Run the same command, expect PASS.** Leave the files uncommitted.

---

### Task 2: The "applied" signal

**Files:**
- Modify: `main/api/PreCondition.java`, `main/server/parser/GenericCommand.java`, `main/server/condition/ClickedCondition.java`
- Test: `test/server/parser/GenericCommandExecuteTest.java` (add), `test/server/condition/ClickedConditionTest.java` (add)

**Interfaces:**
- Consumes: Task 1 `GameContext.markClickHandled()`, `isClickAt`
- Produces: `default void commandApplied() {}` on `PreCondition` — "called once on every precondition of a command whose preconditions all passed". `GenericCommand.execute()` calls it on each of its preconditions once they passed, before its actions run. `NotCondition` does **not** override it, so `NOT_CLICKED` never counts as a hit. `ClickedCondition.commandApplied()` calls `gameContext.markClickHandled()` when `isClickAt(x, y)`.

- [ ] **Step 1: Write the failing tests**
  - `GenericCommandExecuteTest`: `execute_whenAllPreconditionsPassAndActionsSucceed_callsCommandAppliedOnEachPrecondition`; `execute_whenAPreconditionFails_doesNotCallCommandApplied`; `execute_whenAnActionFails_doesNotCallCommandApplied` (Mockito mocks of `PreCondition`/`Action`, verify with `never()`); `execute_whenABreakActionEndsTheList_stillCallsCommandApplied`.
  - `ClickedConditionTest`: `commandApplied_marksTheClickHandled_whenTheClickIsAtItsCell`; `…_doesNothing_whenTheClickIsElsewhere`; `NotCondition(clicked).commandApplied()` does not mark (real `GameContext`).

- [ ] **Step 2: Run, expect FAIL** — `mvn test -Dtest='GenericCommandExecuteTest,ClickedConditionTest'`
- [ ] **Step 3: Implement** the hook as specified; `GenericCommand` calls it just before `return result` in the success path.
- [ ] **Step 4: Run, expect PASS.** Also run `mvn test -Dtest='GenericCommand*Test,*ConditionTest'` to prove no other condition is affected.

---

### Task 3: `GameLoop.processClick`

**Files:**
- Modify: `main/server/engine/GameLoop.java`
- Test: `test/server/engine/ClickGameLoopTest.java` (new; build the real `Workflow`, `GameContext`, `VariableProvider`, `GameLoop` as `TurnsGameLoopTest` does)

**Interfaces:**
- Consumes: Task 1 `GameContext.beginClick/endClick/isClickHandled`, Task 2 hook, `Workflow.addProcess(GenericCommandDescription, Command)`
- Produces: `public CommandOutcome processClick(int aColumn, int aRow)` on `GameLoop`

Behaviour: `beginClick`; run `gameContext.runProcesses()`; if `isClickHandled()` increment `VariableProvider.TURNS_VARIABLE_NAME`, otherwise `tell(SystemMessageKey.SM6.defaultText())`; then `tell` the Processes' message; `endClick()` in a `finally`; catch `QuitException` (tell its message, return `QUIT`) and `UnresolvedReferenceException` (tell, `CONTINUE`) exactly as `processCommand` does, let `ReloadAdventureException` propagate. Returns `CONTINUE` otherwise.

- [ ] **Step 1: Write the failing tests** in `ClickGameLoopTest` (the Process under test: a `GenericCommand` with an empty-verb `GenericCommandDescription`, a `ClickedCondition(3, 4, ctx)` and a message action):
  - `click_onTheCell_runsTheActionsAndCountsATurn` (message told once, TURNS 0 → 1, no SM6)
  - `click_onAnotherCell_tellsSm6AndDoesNotCountATurn` (SM6 told, TURNS stays 0, action not run)
  - `click_whenTheClickedProcessHasAFailingOtherGuard_isAMiss`
  - `click_withOnlyANotClickedProcessMatching_isAMiss`
  - `click_alsoRunsProcessesWithoutClicked_andTheirMessageFollowsSm6OnAMiss`
  - `click_doesNotSeeTheLastTypedCommand` (call `ctx.setCurrentPreposition("on")` before the click, as a typed turn would have left it; a Process gated on `PrepositionCondition("on", ctx)` must not run)
  - `typedTurnAfterAClick_neverSeesClickedAsTrue` (click, then `ctx.isClickAt(3, 4)` is false and `check()` of the condition FAILURE)
  - `click_whenAProcessQuits_returnsQuitAndClearsTheClick` (action throws `QuitException`; `isClickAt` false afterwards)
  - `click_whenAProcessThrowsUnresolvedReference_tellsItAndContinues`
  - `click_endsWithTheClickCleared_afterHitAndAfterMiss`

- [ ] **Step 2: Run, expect FAIL** — `mvn test -Dtest=ClickGameLoopTest`
- [ ] **Step 3: Implement `processClick`.** Add `// TODO: Review needed — TURNS is increased after the Processes ran (a hit is only known then), so a Turns action inside the clicked Process shows the count without this click; a typed command counts before it runs.`
- [ ] **Step 4: Run, expect PASS**, then `mvn test -Dtest='GameLoopTest,TurnsGameLoopTest'` to prove typed play is unchanged.

---

### Task 4: `AdventureRunSession.submitClick`

**Files:**
- Modify: `main/server/engine/AdventureRunSession.java`
- Test: `test/server/engine/AdventureRunSessionTest.java` (add)

**Interfaces:**
- Consumes: Task 3 `GameLoop.processClick(int, int)`
- Produces: `public RunResult submitClick(int aColumn, int aRow)` — same guards as `submit` (superseded → `RunResult(List.of(ENDED_ELSEWHERE_TEXT), true)`; game over → `RunResult(List.of(), true)`), otherwise `runBound(...)`, with the output sink, `gameOver` handling, `ReloadAdventureException` guard and prompt shared with `playTurn`

- [ ] **Step 1: Write the failing tests** (mock `GameLoop` like the existing tests): `submitClick_runsTheLoopWithTheCell_andReturnsItsLinesAndThePrompt`; `submitClick_whenTheLoopQuits_setsGameOverAndAsksForNothing`; `submitClick_afterGameOver_doesNotRunTheLoop`; `submitClick_afterSupersede_answersEndedElsewhere_andDoesNotRunTheLoop`; `submitClick_clearsTheOutputSink`.
- [ ] **Step 2: Run, expect FAIL** — `mvn test -Dtest=AdventureRunSessionTest`
- [ ] **Step 3: Implement.** Extract the body of `playTurn` into `private RunResult play(Supplier<GameLoop.CommandOutcome> aStep)`; `playTurn` and the new click path both call it.
- [ ] **Step 4: Run, expect PASS** (the old `submit_*` tests included).

---

### Task 5: `PictureGrid` and the clickable picture in the run view

**Files:**
- Create: `main/view/support/PictureGrid.java`
- Modify: `main/view/adventure/AdventureRunView.java`, `main/view/location/LocationMapView.java` (use `PictureGrid.SIZE` instead of its private `GRID_SIZE`)
- Test: `test/view/support/PictureGridTest.java` (new), `test/view/adventure/AdventureRunViewTest.java` (add)

**Interfaces:**
- Produces: `PictureGrid` (final, no instances) with `static final int SIZE = 10`, `record Cell(int column, int row)` and
  `static Optional<Cell> cellAt(double aOffsetX, double aOffsetY, double aBoxWidth, double aBoxHeight, double aPictureWidth, double aPictureHeight)`:
  the picture is drawn like CSS `object-fit: contain` (scaled by `min(box/picture)` per axis, centred) in the box; a click outside that drawn area, or any size ≤ 0 (picture not loaded), gives `Optional.empty()`; otherwise `column = min(SIZE - 1, floor((offsetX - left) / drawnWidth * SIZE))`, `row` likewise.
- Produces on `AdventureRunView` (package-private, for tests): `void handlePictureClick(double aOffsetX, double aOffsetY, double aBoxWidth, double aBoxHeight, double aNaturalWidth, double aNaturalHeight)` — does nothing without a session; maps through `PictureGrid.cellAt`; ignores empty; else `session.submitClick(column, row)` and renders like typed input.
- Consumes: Task 4 `AdventureRunSession.submitClick`

- [ ] **Step 1: Write the failing tests**
  - `PictureGridTest`: picture and box same shape → `(0,0)` is cell 0,0, `(box-1, box-1)` is 9,9, centre is 5,5; offset exactly at box width → empty; wide box with a square picture (e.g. 1000×500 box, 500×500 picture): offsetX 100 → empty (left margin), offsetX 250 → column 0, offsetX 749 → column 9, offsetX 750 → empty; tall box with a wide picture: top margin ignored the same way; picture size 0 or box size 0 → empty; a 1280×720 picture in a 640×360 box gives the same cells as in a 1280×720 box.
  - `AdventureRunViewTest` (session is a Mockito mock there): `pictureClick_insideThePicture_submitsTheCellToTheSession`; `pictureClick_inTheMargin_doesNotCallTheSession`; `pictureClick_withoutASession_doesNothing`; `pictureClick_whenTheResultIsGameOver_disablesTheInputAndReleasesTheRun` (mirror the typed-input test that asserts this); `pictureClick_rendersTheNarratorLinesAndThePrompt`.
- [ ] **Step 2: Run, expect FAIL** — `mvn test -Dtest='PictureGridTest,AdventureRunViewTest'`
- [ ] **Step 3: Implement `PictureGrid`**, switch `LocationMapView` to `PictureGrid.SIZE`, extract `private void render(RunResult)` from `handleInput` (renders lines + prompt, `refreshPictureDisplay()`, input enabling, release on game over) and use it for both. Register the listener on `pictureDisplay.getElement().addEventListener("click", …)` with `addEventData` for `event.offsetX`, `event.offsetY`, `element.clientWidth`, `element.clientHeight`, `element.naturalWidth`, `element.naturalHeight`, and pass the numbers to `handlePictureClick`. Check the Vaadin 25.2.6 `DomEvent#getEventData` return type in the Vaadin docs (per the project's Vaadin-docs rule) before writing the reader. Add `// TODO: Review needed — a click is not echoed into the message list the way typed input is.`
- [ ] **Step 4: Run, expect PASS**; also `mvn test -Dtest='LocationMapView*Test'`.
- [ ] **Step 5: Manual check** (the browser mapping cannot be unit tested): run the app, open a running adventure with a picture, click the picture corners and the margins of a letterboxed one; use a Process with `CLICKED 0, 0` and one with `CLICKED 9, 9` to confirm the corners hit, and confirm that a click in a margin produces no output at all (not even SM6). Report what was verified; if the browser cannot be driven, say so.

---

### Task 6: Authoring — editor, selector, formatter

**Files:**
- Create: `main/view/command/condition/ClickedConditionEditor.java`
- Modify: `main/view/command/condition/ConditionSelector.java`, `main/view/command/condition/ConditionListEditor.java`, `main/view/command/PreconditionActionEditor.java`, `main/view/workflow/SingleCommandEditorView.java`, `main/view/command/PreconditionActionFormatter.java`
- Test: `test/view/command/condition/ClickedConditionEditorTest.java`, `test/view/command/condition/ConditionEditorFactoryTest.java` (add), `test/view/command/condition/ConditionSelectorTest.java` (add; create if absent), `test/view/command/PreconditionActionFormatterConditionsTest.java` (add)

**Interfaces:**
- Consumes: Task 1 `ClickedConditionData`, Task 5 `PictureGrid.SIZE`
- Produces: `@AutoRegisterConditionEditor public class ClickedConditionEditor extends ConditionEditorComponent<ClickedConditionData>` with constructor `(ClickedConditionData)`, two required `IntegerField`s labelled `X (column, 0–9)` and `Y (row, 0–9)` with min 0 / max `PictureGrid.SIZE - 1`, `validate()` marking invalid fields (error text e.g. `Please enter a column between 0 and 9`), `getConditionSummary()` returning `(none)` when either is empty, else `clicked 3, 4`. Follow `ChanceConditionEditor`/`ItemAtConditionEditor` for field wiring.
- Produces: constructors `ConditionSelector(boolean offersClicked)`, `ConditionListEditor(AdventureData, boolean offersClicked)`, `PreconditionActionEditor(AdventureData, boolean offersClicked)`; the existing constructors stay and pass `false`. `SingleCommandEditorView` passes `aCommandListType == CommandListType.PROCESS` (kept in a field, since the editor is built later in `populate`). `CommandEditorView` (location/item commands) keeps the one-argument form.
- Produces: formatter case `ClickedConditionData clicked -> "CLICKED " + num(clicked.getX()) + ", " + num(clicked.getY())`.

- [ ] **Step 1: Write the failing tests**
  - Editor: both fields exist with the exact labels; values load from data and write back on change; `validate()` false for empty, `-1`, `10`, true for `0` and `9`; summary `(none)` / `clicked 3, 4`.
  - Factory: `createEditor(new ClickedConditionData(), adventureData)` is a `ClickedConditionEditor`.
  - Selector: the combo box items contain `Clicked (matches a click on the picture)` only for `new ConditionSelector(true)`; absent for `new ConditionSelector()` and `(false)`; choosing it hands out a `ClickedConditionData`.
  - Formatter: `CLICKED 3, 4`; wrapped in `NotConditionData` gives `NOT_CLICKED 3, 4`; null x gives the same placeholder `num` gives elsewhere.
  - Workflow editors: in the existing `SingleCommandEditorView` tests (find with `grep -rl SingleCommandEditorView test`) add: the Processes editor's selector offers Clicked, the Responses and Arrival Processes editors' do not.
- [ ] **Step 2: Run, expect FAIL** — `mvn test -Dtest='ClickedConditionEditorTest,ConditionEditorFactoryTest,ConditionSelectorTest,PreconditionActionFormatterConditionsTest,*CommandEditorView*Test'`
- [ ] **Step 3: Implement** as specified; insert the descriptor in `availableTypes()` only when `offersClicked` (the list is sorted case-insensitively afterwards).
- [ ] **Step 4: Run, expect PASS**, then the whole `view` package: `mvn test -Dtest='com.pdg.adventure.view.**'`.

---

### Task 7: Scenario test, documentation, changelog

**Files:**
- Test: `test/server/engine/ClickGameLoopTest.java` (add one scenario)
- Modify: `CHANGELOG.md`, `docs/specs/04-runtime-engine.md`, `docs/specs/07-ui-and-navigation.md`, `docs/handbooks/author/08-commands-actions-and-conditions.md`, `docs/handbooks/author/09-workflow.md`, `docs/handbooks/author/12-testing-and-running.md`, `docs/handbooks/author/appendix-a-action-and-condition-reference.md`; Javadoc of `PreconditionActionFormatter`

- [ ] **Step 1: Write the scenario test** `click_onTheDoor_movesThePlayer_andNothingElseHappens`: two real locations, a Process `CLICKED 3, 4` with a `MovePlayerAction` (build it as `MovePlayerAction`'s own tests do), click 3,4 → player in the other location and the arrival description told; click 5,5 → still there, SM6 told. Run it: `mvn test -Dtest=ClickGameLoopTest`, expect PASS (all code exists now).
- [ ] **Step 2: Update the docs.** Runtime spec: click input path, `processClick`, hit/miss, TURNS, typed-state reset, `commandApplied`. UI spec: clickable picture, grid mapping and letterbox rule, scoped selector, editor. Author handbook: conditions chapter and appendix A (CLICKED with the 10 × 10 grid picture of x/y, example `CLICKED 3, 4` + Move Player), workflow chapter (Processes only; click miss answers SM6; hit counts as a turn), testing chapter (clicking the picture). Update the `PreconditionActionFormatter` Javadoc example list. Add to `CHANGELOG.md` under `### Added`: the CLICKED condition (what it is, Processes only, SM6 on a miss, counts as a turn on a hit).
- [ ] **Step 3: Full verification** — `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn test`; expect all tests to pass (1953 before this work plus the new ones). Report any failure verbatim; do not mark done otherwise.
- [ ] **Step 4: List the changed and new files** for the author's commit (`git status`); do not commit.
