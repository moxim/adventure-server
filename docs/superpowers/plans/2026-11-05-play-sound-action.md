# PLAY Action and Sound Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Authors can upload mp3 sounds to an adventure and play them from commands with a new PLAY action (`PLAY pirate_bay 2`; count 0 stops every copy of that sound).

**Architecture:** Mirrors the picture feature: `SoundData` per file (bytes in a separate document behind a lazy `@DBRef`, so an adventure run never loads audio eagerly), a Sounds menu view + editor view, a `PlayActionData`/`PlayAction`/`PlayActionMapper`/`PlayActionEditor`, and a usage tracker that blocks deleting a used sound. A PLAY is an *event*: the action appends a `SoundInstruction` to a queue on `GameContext`; `AdventureRunView` drains the queue after every render and hands it to a `SoundPlayer` that drives `Audio` elements from JavaScript.

**Tech Stack:** Java 25, Spring Boot, Spring Data MongoDB, Vaadin Flow (server-side views, `Upload`, `Audio`, `DownloadHandler`), JUnit 5, AssertJ, Mockito, Vaadin Browserless testing (`BrowserlessTest`).

**Spec:** `server/docs/superpowers/specs/2026-11-05-play-sound-action-design.md`

## Global Constraints

- All work is in the `server` module; do not touch `api` or `editor` (AGENTS.md).
- Run git and Maven from `server/`. Maven needs `JAVA_HOME=/opt/homebrew/opt/openjdk@25` (JDK 21/26/27 fail). Command form: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=<Class>`. If a compile hook produces a wall of bogus errors right after an edit, re-run once before investigating.
- Check Vaadin APIs with the Vaadin docs MCP / vaadin.com/docs and Spring APIs with Context7, not by grepping JARs.
- Sound files: mp3 only, at most 6 MiB (`6 * 1024 * 1024` bytes); stored content type `audio/mpeg`.
- Sound names: typed by the author, required, unique within an adventure (compare trimmed and case-insensitively).
- PLAY count: whole number 0 to 99. 0 stops all copies of that sound; n >= 1 plays it n times back to back, starting immediately; PLAY never interrupts other sounds; sounds survive location changes.
- PLAY is silent in the transcript, informational only, and always succeeds; a missing sound, a blocked browser or failed playback is never an error.
- Sounds are not saved in a saved game. Sounds stop on game over, quit, leaving the run page and restart in the same view.
- A PLAY arriving before the player's first interaction with the page is silently dropped in the browser (no queue, no retry).
- No mute button, no cap on the number of sounds, no other audio formats.
- Where something is ambiguous: ask, and mark it `// TODO: Review needed — [reason]` (AGENTS.md).
- Commits are made by the user, not by agents. Each task ends with a suggested conventional-commit message; do not run `git commit`.

## Review Focus

Failure modes the spec implies; each has a test in the named task.
1. `PlayActionData` with null `soundId`, null/negative/over-99 `times` (hand-edited or imported data): nothing is queued, no exception, still SUCCESS (Task 2).
2. A PLAY for a sound id that no longer exists in the adventure: nothing played, no exception, nothing in the transcript (Task 6).
3. An upload that is not really an mp3 (a PNG named `x.mp3`, an AAC/ADTS file, an empty file), and the size boundary exactly 6 MiB (accepted) vs 6 MiB + 1 (refused) (Task 4).
4. A name equal to another sound's name apart from case or surrounding spaces is refused; saving a sound under its own name is allowed (Task 4).
5. Deleting a sound used by a PLAY inside an item's command, an arrival process, a response or a workflow list is refused with the usage listed (Task 3 and Task 5).

## File Structure

Create (package `com.pdg.adventure` under `server/src/main/java`, tests mirror under `server/src/test/java`):
- `model/SoundData.java`, `model/SoundContentData.java`, `model/SoundInstruction.java`, `model/action/PlayActionData.java`
- `server/storage/repository/SoundRepository.java`, `server/storage/repository/SoundContentRepository.java`, `server/storage/service/SoundService.java`
- `server/action/PlayAction.java`, `server/mapper/action/PlayActionMapper.java`
- `view/command/action/PlayActionEditor.java`
- `view/sound/`: `SoundsMainLayout`, `SoundMenuView`, `SoundEditorView`, `SoundViewModel`, `SoundUsageTracker`, `Mp3Format`, `SoundPlayButton`
- `view/adventure/SoundPlayer.java`
- docs: `server/docs/handbooks/author/11a-sounds.md`

Modify: `model/AdventureData.java`, `server/engine/GameContext.java`, `server/engine/AdventureRunSessionFactory.java` (clearRegistries), `view/support/RouteIds.java`, `view/command/action/ActionSelector.java`, `view/command/PreconditionActionFormatter.java`, `view/adventure/AdventureEditorView.java`, `view/adventure/AdventureRunView.java`, `server/docs/handbooks/author/appendix-a-action-and-condition-reference.md`, `server/docs/specs/02-functional-requirements.md`, `CHANGELOG.md` (repo root of `server/`).

---

### Task 1: Sound data model and persistence

**Files:**
- Create: `model/SoundData.java`, `model/SoundContentData.java`, `server/storage/repository/SoundRepository.java`, `server/storage/repository/SoundContentRepository.java`, `server/storage/service/SoundService.java`
- Modify: `model/AdventureData.java` (field `soundData`)
- Test: `server/storage/service/SoundServiceTest.java`, `model/AdventureDataSoundTest.java`

**Interfaces:**
- Produces:
  - `SoundData extends BasicData` (`@Document("sounds")`, Lombok `@Data`, `@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)` like `PictureData`): `String adventureId; String name; String contentType; long sizeBytes;` and `@DBRef(lazy = true) @CascadeDelete SoundContentData content;`.
  - `SoundContentData` (`@Document("soundcontent")`, `@Data`): `@Id String id; byte[] content;`. Its id is generated by the existing `UuidIdGenerationMongoEventListener` only for `Ided` types, so `SoundService` sets the id (`Ulid.fast().toString()`) itself.
  - `AdventureData.getSoundData(): Map<String, SoundData>` — `@DBRef(lazy = false) @CascadeDelete`, initialised to `new HashMap<>()` in the constructor (documents saved before sounds existed must read as an empty map). Only metadata is eager; the bytes sit behind the lazy `content` reference.
  - `SoundService(SoundRepository, SoundContentRepository, CascadeDeleteHelper)` with `void save(SoundData aSound, byte[] aNewContentOrNull)`, `void delete(String aSoundId)`, `static boolean isNameTaken(Map<String, SoundData> someSounds, String aName, String anExceptSoundId)`.

- [ ] **Step 1: Write the failing tests.** `SoundServiceTest` (Mockito mocks for the repositories/helper): `save_withNewContent_savesContentBeforeSoundAndLinksIt` (content saved first, `sound.getContent()` non-null with the saved id, `sound.getSizeBytes()` equals the byte length, `contentType` is `audio/mpeg`); `save_withNullContent_leavesTheStoredContentUntouched` (content repository never called); `delete_cascadesThenDeletesTheSound` (helper `cascadeDelete(sound)` called before `soundRepository.delete(sound)`; unknown id only logs); `isNameTaken_ignoresCaseAndSurroundingSpaces` (`"Pirate_Bay "` vs existing `"pirate_bay"` → true); `isNameTaken_allowsTheSoundsOwnName` (same id passed as `anExceptSoundId` → false). `AdventureDataSoundTest`: `newAdventure_hasEmptySoundData`.
- [ ] **Step 2: Run** `mvn -q test -Dtest=SoundServiceTest,AdventureDataSoundTest`. Expected: compilation failure / FAIL (classes missing).
- [ ] **Step 3: Implement** the types above. Follow `PictureData`/`PictureRepository` (`@Repository`, `MongoRepository<…, String>`); follow `AdventureService.savePictureData/deletePicture` for logging style. `delete` uses `CascadeDeleteHelper.cascadeDelete`, which already resolves a lazy proxy (so the content document goes with the sound).
- [ ] **Step 4: Run** the same command. Expected: PASS.
- [ ] **Step 5: Confirm the cascade on adventure delete** by adding `AdventureDataSoundTest.soundData_isMarkedForCascadeDeleteAndEagerReference` which reflects on the field and asserts the `@CascadeDelete` and `@DBRef(lazy = false)` annotations are present, then re-run.
- Suggested commit: `feat: add SoundData model and SoundService`

### Task 2: PlayAction, mapper and engine queue

**Files:**
- Create: `model/SoundInstruction.java`, `model/action/PlayActionData.java`, `server/action/PlayAction.java`, `server/mapper/action/PlayActionMapper.java`
- Modify: `server/engine/GameContext.java`, `server/engine/AdventureRunSessionFactory.java` (`clearRegistries`)
- Test: `server/engine/GameContextSoundTest.java`, `server/action/PlayActionTest.java`, `server/mapper/action/PlayActionMapperTest.java`, extend `server/engine/AdventureRunSessionFactoryTest.java`

**Interfaces:**
- Produces:
  - `record SoundInstruction(String soundId, int times)` in `model` (like `PictureOverlayData`). `times == 0` means stop.
  - `PlayActionData extends ActionData`: `String soundId; Integer times;` (Lombok like `PictureActionData`).
  - `GameContext`: `void playSound(String aSoundId, int aTimes)` (ignores a null/blank id and a `aTimes` outside 0..99), `List<SoundInstruction> drainSoundInstructions()` (returns the queued instructions oldest first and empties the queue), `void clearSoundInstructions()`.
  - `PlayAction(String aSoundId, Integer aTimes, GameContext aGameContext)`: `@Getter`, `@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)`, `isInformationalOnly()` true, `execute()` calls `gameContext.playSound(...)` when `aTimes != null` and returns `CommandExecutionResult(State.SUCCESS)` always.
  - `PlayActionMapper`: `@Service @AutoRegisterMapper(priority = 30, description = "Play action mapper")`, copy of `PictureActionMapper` (both directions implemented).

- [ ] **Step 1: Write the failing tests.** `GameContextSoundTest`: `playSound_queuesInstructionsOldestFirst`; `drain_returnsAndEmptiesTheQueue`; `playSound_ignoresBlankIdAndOutOfRangeCounts` (null id, `""`, times `-1`, `100` → queue empty); `queue_survivesChangingTheLocationAndThePicture` (set another location/picture, queue unchanged); `clearSoundInstructions_emptiesTheQueue`. `PlayActionTest` (mock `GameContext`): `execute_queuesTheSound_andSucceeds`; `execute_withNullTimes_queuesNothing_andStillSucceeds`; `isInformationalOnly_isTrue`. `PlayActionMapperTest`: `roundTrip_keepsSoundIdAndTimes`; `mapToBO_withNullSoundId_stillBuildsAnAction`. Factory test: `clearRegistries` leaves `gameContext.drainSoundInstructions()` empty after a restart.
- [ ] **Step 2: Run** `mvn -q test -Dtest=GameContextSoundTest,PlayActionTest,PlayActionMapperTest,AdventureRunSessionFactoryTest`. Expected: FAIL.
- [ ] **Step 3: Implement.** The queue is a plain `ArrayList` in `GameContext` (the context is already scoped per browser session; no extra synchronisation). Call `clearSoundInstructions()` in `AdventureRunSessionFactory.clearRegistries()` next to `setCurrentPictureId(null)`.
- [ ] **Step 4: Run** the same command. Expected: PASS. Also run `mvn -q test -Dtest='*Mapper*Test'` to confirm the auto-registration still finds every mapper.
- Suggested commit: `feat: add PlayAction with a sound instruction queue`

### Task 3: PLAY in the command editor, formatter and usage tracker

**Files:**
- Create: `view/command/action/PlayActionEditor.java`, `view/sound/SoundUsageTracker.java`
- Modify: `view/command/action/ActionSelector.java` (after the "Overlay" descriptor), `view/command/PreconditionActionFormatter.java`
- Test: `view/command/action/PlayActionEditorTest.java`, `view/command/PreconditionActionFormatterActionsTest.java` (extend), `view/sound/SoundUsageTrackerTest.java`, extend `view/command/action/ActionEditorFactoryTest.java`

**Interfaces:**
- Consumes: `PlayActionData`, `SoundData`, `AdventureData.getSoundData()`.
- Produces:
  - `@AutoRegisterActionEditor public class PlayActionEditor extends ActionEditorComponent<PlayActionData>` with constructor `(PlayActionData, AdventureData)`: a `ComboBox<SoundData>` ("Sound", items sorted by name case-insensitively, label = name, required) and an `IntegerField` ("Times", min 0, max 99, step 1, required, helper text "0 stops this sound"). `validate()` is true only with a sound selected and a count 0..99 (sets error messages like `PictureActionEditor`); `getActionSummary()` returns `"<name> <times>"` or `"(none)"`.
  - `ActionSelector` descriptor: `new ActionTypeDescriptor("Play", "Play a sound, or stop it with 0", PlayActionData::new)`.
  - Formatter case: `case PlayActionData p -> "PLAY " + resolveSoundName(p.getSoundId()) + " " + num(p.getTimes())` (unknown sound shows `?`, like `resolvePictureName`; build `soundsById` in the constructor from `adventureData.getSoundData()`, null-safe).
  - `SoundUsageTracker` mirroring `PictureUsageTracker`: `static List<SoundUsage> findSoundUsages(AdventureData, String soundId)`, `static int countSoundUsages(AdventureData, String soundId)`, nested `SoundUsage implements TrackedUsage` (display text names the action, its number in the command, the command and the origin, as `PictureUsage` does). It scans only `PlayActionData` through `ActionScanner.scan`.

- [ ] **Step 1: Write the failing tests.** `PlayActionEditorTest` (pattern of `DescribeActionEditorTest`): `preselectsTheStoredSoundAndTimes`; `validate_isFalseWithoutASound`; `validate_isFalseForTimesOutsideZeroToNinetyNine` (-1, 100); `validate_acceptsZero`; `changingTheSelection_updatesTheActionData`. Formatter test: `play_showsNameAndCount` (`"PLAY pirate_bay 2"`) and `play_withUnknownSound_showsQuestionMark`. `SoundUsageTrackerTest`: usages found for a PLAY in a location command, in an item's command, in an arrival process, in a response and in a workflow list (five cases); `count_isZeroForAnUnusedSound`. Factory test: `createEditor_withPlayActionData_shouldReturnPlayActionEditor`.
- [ ] **Step 2: Run** the new/extended test classes. Expected: FAIL.
- [ ] **Step 3: Implement** the editor by copying `PictureActionEditor` plus the number field; the tracker by copying the structure of `PictureUsageTracker` for its action part only.
- [ ] **Step 4: Run** the same classes. Expected: PASS.
- Suggested commit: `feat: author the PLAY action in the command editor`

### Task 4: Sound editor view (upload, rename, replace)

**Files:**
- Create: `view/sound/SoundsMainLayout.java`, `view/sound/SoundEditorView.java`, `view/sound/SoundViewModel.java`, `view/sound/Mp3Format.java`, `view/sound/SoundPlayButton.java`
- Modify: `view/support/RouteIds.java` (add `SOUND_ID("soundId")`)
- Test: `view/sound/Mp3FormatTest.java`, `view/sound/SoundEditorViewBrowserlessTest.java`

**Interfaces:**
- Consumes: `SoundService`, `AdventureService.saveAdventureData`, `AdventureAccessService`, `AdventureRouteResolver.resolveAdventureOrForward`, `HistoryNavigation`.
- Produces:
  - `Mp3Format`: `static final int MAX_BYTES = 6 * 1024 * 1024`; `static boolean looksLikeMp3(byte[] someBytes)` — true for a leading `ID3` tag, or an MPEG-audio frame sync (`0xFF` then `(b1 & 0xE0) == 0xE0`) whose layer bits say Layer III (`((b1 >> 1) & 0x03) == 0x01`); false for anything else, including null and arrays shorter than 3 bytes.
  - `SoundsMainLayout extends AdventureAppLayout` (`createDrawer("Sounds")`, `setPrimarySection(Section.NAVBAR)`) as `PicturesMainLayout`.
  - `SoundViewModel` (id, name, adventureId) as `PictureViewModel`.
  - `@Route("author/adventures/:adventureId/sounds/:soundId/edit", layout = SoundsMainLayout.class) @RouteAlias("author/adventures/:adventureId/sounds/new", …) @RolesAllowed("ROLE_AUTHOR") SoundEditorView(SoundService, AdventureService, AdventureAccessService)`: name `TextField` (required; unique via `SoundService.isNameTaken(adventureData.getSoundData(), name, soundId)`, message "A sound with this name already exists"), `Upload` (`setAcceptedFileTypes("audio/mpeg", ".mp3")`, `setMaxFileSize(Mp3Format.MAX_BYTES)`, `setMaxFiles(1)`), a `SoundPlayButton` for the staged or stored bytes, `ResetBackSaveView` buttons, a Delete button that is only enabled for a stored sound. Package-private `void handleUpload(UploadEvent)` and `void stagePendingUpload(byte[])` for browserless tests, as in `PictureEditorView`. Save = `soundService.save(sound, pendingBytesOrNull)`, `adventureData.getSoundData().put(id, sound)`, `adventureService.saveAdventureData(adventureData)`.
  - `SoundPlayButton extends Button` with constructor `(Supplier<byte[]> aBytes)`: first click plays through a hidden `Audio` fed by a `DownloadHandler` (content type `audio/mpeg`), second click stops; icon switches play/stop. (Confirm in the Vaadin docs that `Audio` accepts a `DownloadHandler` source; if not, use a `StreamResource`.)

- [ ] **Step 1: Write the failing tests.** `Mp3FormatTest`: `id3Header_isAccepted`; `mpegLayer3FrameSync_isAccepted` (`FF FB 90 00`); `aacAdtsHeader_isRejected` (`FF F1 …`); `pngHeader_isRejected`; `empty_null_andTwoByteInputs_areRejected`; `maxBytes_isSixMebibytes`. `SoundEditorViewBrowserlessTest` (pattern of `PictureEditorViewBrowserlessTest`): `upload_ofAPngNamedMp3_isRefusedWithANotification`; `upload_ofExactlySixMebibytes_isAccepted` and `upload_ofSixMebibytesPlusOne_isRefused` (use a valid ID3 prefix padded to size); `save_isDisabledWithoutNameOrFile`; `duplicateName_differingOnlyByCaseOrSpaces_isRefused`; `savingASoundUnderItsOwnName_isAllowed`; `save_storesTheSoundInTheAdventureAndCallsTheServices` (verify `soundService.save`, `adventureService.saveAdventureData`); `renamingWithoutANewFile_savesWithNullContent` (verify `soundService.save(sound, null)`).
- [ ] **Step 2: Run** `mvn -q test -Dtest=Mp3FormatTest,SoundEditorViewBrowserlessTest`. Expected: FAIL.
- [ ] **Step 3: Implement.** Copy the structure of `PictureEditorView` (binder, status listener, `beforeEnter`/`beforeLeave`, `setData`); replace the picture sniff by `Mp3Format.looksLikeMp3`; failure messages: "Sounds must be mp3 files of at most 6MB" and "The uploaded file is not a valid mp3 file."
- [ ] **Step 4: Run** the same command. Expected: PASS.
- Suggested commit: `feat: add the sound editor view`

### Task 5: Sounds menu view and adventure editor entry

**Files:**
- Create: `view/sound/SoundMenuView.java`
- Modify: `view/adventure/AdventureEditorView.java` (field `numberOfSounds`, "Sounds" button, `SubMenuButtons` record gets `Button editSoundsButton`, `fillGui` sets the count, stats row and menu column include them)
- Test: `view/sound/SoundMenuViewBrowserlessTest.java`, extend `view/adventure/AdventureEditorViewRoutingTest.java`

**Interfaces:**
- Consumes: `SoundEditorView`, `SoundUsageTracker.countSoundUsages/findSoundUsages`, `SoundPlayButton`, `SoundService.delete`.
- Produces: `@Route("author/adventures/:adventureId/sounds", layout = SoundsMainLayout.class) @PageTitle("Sounds") @RolesAllowed("ROLE_AUTHOR") SoundMenuView(SoundService, AdventureService, AdventureAccessService)` — search field, "Create Sound" / "Edit Sound" / "Back" buttons, count label ("Sounds: n"), a grid with columns Play (`SoundPlayButton`), Name (sortable), Size (KB), Used; double-click edits; context menu Edit / Find Usage / Delete. Package-private `void confirmDeleteSound(SoundData)` and `ConfirmDialog buildDeleteConfirmDialog(SoundData)` as in `PictureMenuView`. Delete removes the sound from `adventureData.getSoundData()`, calls `adventureService.saveAdventureData` and `soundService.delete`.

- [ ] **Step 1: Write the failing tests** (pattern of `PictureMenuViewBrowserlessTest`): `grid_listsTheAdventuresSounds_withNameSizeAndUsedCount`; `search_filtersByNameIgnoringCase`; `delete_ofAnUnusedSound_removesItFromTheAdventureAndTheStore`; `delete_ofASoundUsedByAPlayInAnArrivalProcess_isRefusedAndShowsUsage` (verify `soundService.delete` never called, usage dialog opened); `playButtonIsPresentForEveryRow`. Adventure editor: `soundsButton_navigatesToTheSoundMenu` and `totalSounds_showsTheNumberOfSounds`.
- [ ] **Step 2: Run** `mvn -q test -Dtest=SoundMenuViewBrowserlessTest,AdventureEditorViewRoutingTest`. Expected: FAIL.
- [ ] **Step 3: Implement** by copying `PictureMenuView`; replace the thumbnail column by the play button column; hint text: "Sounds must be mp3 files, at most 6MB. Double-click a sound to edit it, or right-click for more options." Wire the button into `AdventureEditorView` exactly where the Pictures button sits.
- [ ] **Step 4: Run** the same command. Expected: PASS.
- Suggested commit: `feat: add the sounds menu to the adventure editor`

### Task 6: Playing sounds in the run view

**Files:**
- Create: `view/adventure/SoundPlayer.java`
- Modify: `view/adventure/AdventureRunView.java`
- Test: `view/adventure/SoundPlayerTest.java`, extend `view/adventure/AdventureRunViewTest.java`

**Interfaces:**
- Consumes: `GameContext.drainSoundInstructions()`, `SoundInstruction`, `SoundData`, `AdventureData.getSoundData()`.
- Produces:
  - `SoundPlayer` (package-private final class) constructed with `(BiConsumer<String, Object[]> aJsRunner, Function<String, String> aUrlResolver)`; the view passes `element.executeJs` and a resolver that returns the browser URL of a sound's stream. Methods: `void armFirstInteractionGate()` (JS: sets `window.abSoundsUnlocked = false` and installs one-time capturing `keydown`/`pointerdown`/`touchstart` listeners on `document` that set it true), `void apply(List<SoundInstruction>)` (for each instruction: `times > 0` → JS `abPlay(url, times)`; `times == 0` → JS `abStop(url)`; instructions whose URL resolver returns null are skipped), `void stopAll()` (JS `abStopAll()`).
  - Browser-side behaviour (JS text block in the class, like `FIT_OVERLAY_LAYER`): `abPlay` does nothing while `window.abSoundsUnlocked` is not true; otherwise it creates a new `Audio(url)`, registers it in `window.abSounds[url]` (a Set), plays it `times` times by re-playing on `ended`, and removes it from the set when done; `abStop(url)` pauses and clears every copy in that set (stop is honoured even before the gate opens); `abStopAll()` does so for every url.
  - The URL for a sound comes from a hidden `Audio` component per sound id (created on first use, appended to the view, `setSrc` a `DownloadHandler` that writes `sound.getContent().getContent()` with content type `audio/mpeg`); the resolver returns that element's src attribute value as seen by the browser, so the JS runner receives it through an element reference parameter instead of a string if the Vaadin API requires it. Confirm the exact mechanism in the Vaadin docs.
- `AdventureRunView` changes: create the `SoundPlayer` on attach and call `armFirstInteractionGate()`; after `render(RunResult)` and after `renderOpeningRoom()` call `soundPlayer.apply(session.getGameContext().drainSoundInstructions())` (instructions for a sound id not in `adventureData.getSoundData()` are skipped); call `soundPlayer.stopAll()` when the result is game over, in `onDetach`, and wherever the view starts or replaces a run (the reused-instance reset block near `sessionFactory.release(runOwner)` and the `startReplacingActive` takeover).

- [ ] **Step 1: Write the failing tests.** `SoundPlayerTest` (fake JS runner recording calls, resolver backed by a map): `play_callsAbPlayWithUrlAndTimes`; `zero_callsAbStop`; `unknownSound_isSkippedWithoutError`; `stopAll_callsAbStopAll`; `armGate_installsTheInteractionListenersOnce`; `instructionsKeepTheirOrder`. `AdventureRunViewTest`: `afterATurn_thePendingSoundInstructionsAreHandedToThePlayerAndTheQueueIsEmpty`; `aPlayForADeletedSound_isIgnored_andNothingIsAddedToTheTranscript`; `gameOver_stopsAllSounds`; `restartingTheRunInTheSameView_stopsAllSounds`; `detaching_stopsAllSounds`.
- [ ] **Step 2: Run** `mvn -q test -Dtest=SoundPlayerTest,AdventureRunViewTest`. Expected: FAIL.
- [ ] **Step 3: Implement** `SoundPlayer` and the view wiring as specified. Keep `render(...)` readable: one private `playPendingSounds()` helper.
- [ ] **Step 4: Run** the same command. Expected: PASS.
- [ ] **Step 5: Manual browser check** (JavaScript is not exercised by the browserless tests). Run the app (`/run` skill or the project's usual start), upload two short mp3s, build a Process `PLAY a 2`, a Process `PLAY b 1`, a Response `PLAY a 0`, and test in **Brave and Safari** (Safari is the user's test browser, and the strictest on autoplay). Verify: the opening-room PLAY stays silent; after the first typed input `PLAY a 2` plays twice; a second PLAY overlaps; moving rooms does not stop a sound; `PLAY a 0` stops every copy of a; quitting or leaving the page stops all; a PLAY of a deleted sound is silent. Record what was and wasn't verified in the task report.
- Suggested commit: `feat: play sounds in the adventure run view`

### Task 7: Documentation, changelog and full verification

**Files:**
- Create: `server/docs/handbooks/author/11a-sounds.md`
- Modify: `server/docs/handbooks/author/appendix-a-action-and-condition-reference.md` (add PLAY), `server/docs/handbooks/author/13-tips-and-current-limits.md` (limits: mp3 only, 6 MB, silent before first interaction, no mute, not saved), `server/docs/specs/02-functional-requirements.md` (sounds and PLAY), `CHANGELOG.md` (`## [Unreleased]` → `### Added`, one bullet in the existing style, following `AGENTS-update-changelog.md`)

**Interfaces:** none (documentation only).

- [ ] **Step 1: Write the handbook chapter** `11a-sounds.md` in the style of `11-pictures.md`: uploading (mp3, 6 MB, unique name), the play button, PLAY and its count (0 stops all copies, overlap, sounds survive moving), the first-interaction rule, deleting a used sound.
- [ ] **Step 2: Update** the appendix, limits chapter, functional requirements and CHANGELOG as listed.
- [ ] **Step 3: Run the full suite** with the `verify-tests` skill (or `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test`). Expected: all tests pass; report the pass count and any failure verbatim.
- [ ] **Step 4: Spec check.** Walk the spec's *Acceptance* section line by line and name the test or manual check that covers each line; report any line without one.
- Suggested commit: `docs: document sounds and the PLAY action`

---

## Self-review notes

- **Spec coverage:** PLAY semantics and range (Tasks 2, 3, 6); storage and lazy bytes (Task 1); mp3/6 MiB/name rules (Task 4); delete protection with usage (Tasks 3, 5); menu, editor, play buttons, adventure-editor entry and count (Tasks 4, 5); first-interaction skip and stop-on-end/quit/leave/restart (Task 6); docs and changelog (Task 7); non-goals add nothing.
- **Types:** `SoundInstruction(String soundId, int times)`, `PlayActionData(soundId, times)`, `PlayAction(String, Integer, GameContext)`, `SoundService.save(SoundData, byte[])` and `SoundData.content` are used identically in every task that touches them.
- **Open points to confirm while building (marked as the builder's call in the spec):** the exact Vaadin API for giving the browser a URL of a streamed `Audio` source (Tasks 4 and 6), and whether a lazy single-value `@DBRef` to `SoundContentData` is cascade-deleted by `CascadeDeleteHelper` (Task 1, Step 5 pins the annotations; Task 1's `delete_cascadesThenDeletesTheSound` pins the call).
