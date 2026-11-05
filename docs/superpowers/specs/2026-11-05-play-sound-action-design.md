# PLAY action and sound management — design

Status: draft for approval (2026-11-05). Nothing is built yet.

## Intent

Adventure authors can already show pictures (PICTURE, OVERLAY). They cannot make a sound. This feature lets an
author upload mp3 files to an adventure and play them from the adventure's commands with a new **PLAY** action, e.g.
`PLAY pirate_bay 2` plays the sound named "pirate_bay" twice, back to back, starting right away.

It deliberately follows the existing picture feature end to end: a data object per file, a menu view and an editor view
to manage the files, an action with its own editor, a usage tracker that protects used files from deletion. Where this
document says "like pictures", the builder should open the picture implementation and mirror it.

## Behaviour (decided with the product owner)

### The PLAY action
- Arguments: a sound (chosen by the author in the action editor, stored as the sound's **id**) and a whole number.
- Number **n >= 1**: play the sound n times in a row, starting immediately.
- Number **0**: stop that sound — **all copies of it** currently playing. Other sounds are not touched.
- PLAY never interrupts anything. A new sound plays on top of whatever is already playing. PLAY of a sound that is
  already playing starts another copy on top of it.
- Sounds **keep playing when the player moves to another location**. This is why PLAY 0 exists.
- PLAY is silent in the transcript: it produces no text and always succeeds (like PICTURE and OVERLAY, it is
  informational only). A PLAY for a sound that no longer exists does nothing and still succeeds.
- Sounds are **not** part of a saved game; loading a saved game does not restart sounds.
- Everything stops when the game ends, when the player quits, when the player leaves the run page, and when the
  adventure is restarted in the same view.

### Browser sound restrictions
Browsers refuse to play sound before the player has interacted with the page (Safari most strictly). A PLAY that fires
before the player's first interaction (typing in the input, clicking the picture or a button) is **silently skipped**:
no retry, no queueing, no message. The opening room's PLAY is therefore normally skipped.

### Sound files
- mp3 only, at most **6 MB**.
- Each sound has a **name**, typed in by the author at upload, **unique within the adventure**.
- Sounds belong to one adventure and are managed per adventure, like pictures.
- A sound cannot be deleted while a PLAY action uses it; the author is shown where it is used (like pictures).
- Deleting an adventure deletes its sounds (like pictures).

### Author UI
- A **Sounds** menu view per adventure (list, search, create, edit, back, count), reached from the adventure editor
  like the Pictures menu, with a "Total Sounds" read-only field there like "Total Pictures".
- A sound **editor view** to upload (name + file) and delete.
- A **play button for every sound in the list** and on the upload form (to check a file before saving it).
- A **PLAY action editor** in the command editor: a combo box of the adventure's sounds by name, and a number field.
  Registered in the action selector with a one-line description. The action summary reads `PLAY <name> <n>`.
- Roles and access checks as for pictures (ROLE_AUTHOR + the adventure access service).

## Goals
- An author can add sound to an adventure without leaving the editor.
- The feature reads and behaves like the picture feature, so there is little new to learn and to maintain.

## Non-goals (not now)
- A mute button or volume control for the player. (Explicitly declined.)
- A limit on the number of sounds per adventure. (Explicitly declined.)
- Other audio formats, looping forever, fades, per-location ambience, volume per PLAY.
- Remembering or restoring sounds across save/load.
- Any prompt or "click to start" screen to get around the browser restriction.

## Anti-goals
- A PLAY must never break or slow a turn: a missing sound, a blocked browser or a failing playback is silent, never an
  error shown to the player or an exception in the engine.
- Running an adventure must not load the audio bytes of every sound into memory (see "Loading" below).

## Constraints
- All work is in the `server` module (see AGENTS.md); do not touch `api` or `editor`.
- Java/Vaadin/Spring Boot conventions of the project (AGENTS-conventions, -architecture, -views, -testing).
- Where an ambiguity is met, ask and mark it with `// TODO: Review needed — [reason]`.
- Update the author handbook, the functional requirements spec and the CHANGELOG as the project's rules require
  (AGENTS-update-changelog.md).

## Where the existing picture feature lives (for the builder)
- Data: `PictureData` (`@Document("pictures")`: adventureId, name, content, contentType) referenced from
  `AdventureData.pictureData` with `@DBRef` + `@CascadeDelete`; `PictureRepository`; `AdventureService`
  save/delete methods.
- Views: `view/picture/` — `PictureMenuView`, `PictureEditorView`, `PicturesMainLayout`, `PictureViewModel`,
  `PictureUsageTracker`; entry from `AdventureEditorView`.
- Action: `PictureActionData` (model), `PictureAction` (server/action, informational only, calls the game context),
  `PictureActionMapper` (`@AutoRegisterMapper`), `PictureActionEditor`, entry in `ActionSelector`, summary in
  `PreconditionActionFormatter`, usage via `ActionScanner`.
- Run view: `AdventureRunView.refreshPictureDisplay()` and `streamOf(PictureData)` after every `render(...)`.
- Upload checks in `PictureEditorView`: `Upload` limits plus a server-side content sniff on the received bytes.

## Decisions left to the builder (my calls, flagged for review)
1. **Size limit: 6 MiB** (6 * 1024 * 1024), following the picture editor which treats "2MB" as 2 MiB.
2. **mp3 check:** client-side accepted type `audio/mpeg` / `.mp3`, plus a server-side check of the received bytes
   (an `ID3` tag or an MPEG audio frame sync at the start), like the picture content sniff. Stored content type is
   `audio/mpeg`.
3. **Number range:** whole numbers 0 to 99; the editor rejects anything else. (The product owner gave no upper bound.)
4. **Editing a sound:** the name can be changed (still unique) and a new file can replace the content, as in the
   picture editor. Because PLAY stores the id, renaming never breaks an action.
5. **Loading:** sounds are stored like pictures, as separate documents (one collection, e.g. `sounds`), referenced from
   the adventure. The audio bytes must be fetched only when the browser asks for a sound (a streamed download handler,
   as `streamOf` does for pictures), not eagerly with the adventure. How the metadata/bytes split is done (lazy
   reference, a metadata-only projection, GridFS) is the builder's choice, as long as an adventure run does not pull
   all audio into memory.
6. **Engine to browser:** a PLAY is an *event*, not state like the picture. The suggested shape: the action appends a
   play/stop instruction (sound id, count) to a queue on the game context; the run view drains the queue after each
   `render(...)` and sends the instructions to the browser, which plays them with audio elements it keeps track of per
   sound id (so PLAY 0 can stop all copies). The browser side keeps an "interacted yet" flag set by the first key,
   pointer or touch event on the page and drops instructions that arrive before it is set.
7. **Test runs** (the author's "Test:" origin of the run view) play sound exactly like library runs.
8. **Usage tracker:** a `SoundUsageTracker` scanning `PlayActionData` through `ActionScanner`, mirroring
   `PictureUsageTracker`, used by the Sounds menu to block deletion and show where a sound is used.

## Acceptance (what "done" means)
- An author can upload an mp3 (<= 6 MB, unique name), hear it with the list's play button, and delete it when unused.
- Uploading a non-mp3, an oversized file, or a duplicate name is refused with a clear message.
- Deleting a sound used by a PLAY action is refused and shows where it is used.
- A PLAY action can be added in a command, shows as `PLAY <name> <n>`, and survives save and reload.
- In a run, after the player's first input, `PLAY x 2` plays sound x twice; a second PLAY overlaps; `PLAY x 0` stops
  every copy of x; moving to another location does not stop a sound; ending, quitting, leaving or restarting does.
- A PLAY before the player's first interaction is skipped without any message.
- The picture feature and everything else behave as before; the test suite passes.
