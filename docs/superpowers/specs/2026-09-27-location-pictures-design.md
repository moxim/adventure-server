# Location pictures: author-managed images shown during play

## Goal

Authors can attach a picture to a location and to individual actions. During
play the picture renders in the top half of the screen, above the existing
chat transcript, at three specific moments:

1. The player arrives at a location for the **first time** (`timesVisited == 0`).
2. The player explicitly **looks** at the current location (`look`/`l`/`desc`/
   `examine`/`x` — all synonyms of the built-in `describe` verb).
3. A new **PICTURE** action fires, naming a specific picture to show.

Pictures are managed in a new `PictureMenuView`, where an author can upload,
name, and delete images. This is the first place in the codebase accepting
user-uploaded binary content — no `Upload` component, GridFS, or blob storage
exists today; all current "images" are static classpath resources referenced
by path (`AdventureAppLayout.java:58`, `LocationMapView.java:10-27`).

## Non-goals

- No image editing/cropping/resizing UI — authors upload a finished image.
- No per-adventure picture size/quota enforcement beyond what Mongo already
  imposes (see Open questions).
- No animation/slideshow — one static image at a time.
- Multiple pictures per location are out of scope; a location has at most one
  default picture (see Decision 2 below).

## Decisions made during design

These were resolved conversationally before this spec was written; each
carries the trade-off that was rejected, for future reference.

1. **Storage: MongoDB `byte[]`, own top-level `pictures` collection**, not
   embedded in `AdventureData` and not filesystem+path. Rejected embedding in
   `AdventureData.pictures : Map<String,PictureData>` (mirroring the existing
   `messages` map) because that document is a single Mongo document with
   `@CascadeSave`d children (`AdventureData.java`) — piling binary content
   into it risks the 16MB Mongo document cap and bloats every adventure
   load/save. A separate `pictures` collection mirrors how `locations` is
   already its own top-level collection rather than embedded.
2. **A location has one optional default picture; `PICTURE` is a same-turn
   override.** Rejected "no location-level picture, only ever shown via
   `PICTURE` actions placed everywhere" as needlessly repetitive for authors.
3. **Deleting a picture still referenced by a location or `PictureActionData`
   is blocked**, with the referencing locations/actions listed in the
   dialog — matches the existing usage-guard pattern (`LocationUsageTracker`).
   Rejected silent orphaning (dangling `pictureId` treated as "no picture" at
   runtime) as something that lets an author silently break a scene.
4. **A too-dark-to-see location shows no picture**, mirrored on the same gate
   that already suppresses `getLongDescription()`
   (`Location.java:130-149`, `isTooDarkToSee()`).
5. **Runtime display is a same-turn "flash," not a persistent overlay that
   survives until explicitly replaced.** Originally designed as a persistent
   `GameContext.currentPictureId` that stayed put across turns until a move
   or another `PICTURE` action changed it. Revised after the requirement that
   pictures only show on first-visit arrival, on look, and on `PICTURE` — a
   persistent field would keep the arrival picture on screen through
   unrelated subsequent turns (e.g. "take sword"), which is not what was
   asked for. The final mechanism resets to "no picture" once per sub-command
   and only the three named triggers populate it (see Section C).
6. **The long/short description decision already made by `Location` is the
   single source of truth for the picture trigger, rather than a duplicated
   "is this a picture moment" check.** Originally planned as a brand-new
   internal `ShowLocationPictureAction` wired into `CommandFactory`'s built-in
   look Response, run alongside a special-cased `timesVisited == 0` check in
   `MovePlayerAction`. Simplified once it was noticed both call sites already
   decide long-vs-short description at exactly the moments a picture should
   show — see Section C.

## A. Data model

New `PictureData` (`src/main/java/com/pdg/adventure/model/PictureData.java`):

```java
@Data
@Document(collection = "pictures")
public class PictureData extends BasicData {
    private String adventureId;
    private String name;
    private byte[] content;
    private String contentType;
}
```

- `id` (ULID) comes from `BasicData`, same as `LocationData`/`MessageData`.
- New runtime `Picture` class (`server/picture/Picture.java`) mirroring the
  DO/BO split used throughout (`Location`/`LocationData`,
  `Action`/`ActionData`): `id`, `name`, `content`, `contentType`, no
  behavior beyond exposing bytes for rendering.
- New `PictureMapper` (`server/mapper/PictureMapper.java`,
  `@Service @AutoRegisterMapper`), same shape as `LocationMapper`.
- New `PictureService`/repository following the existing per-entity
  service pattern (mirrors whatever `LocationData`'s persistence access
  point is — confirm exact interface name during implementation) for
  CRUD + "list pictures for adventure X" lookups used by the editor
  `ComboBox`es and `PictureMenuView`'s grid.
- `LocationData` gains `private String pictureId;` (nullable). `Location`
  gains a matching field/getter. `LocationMapper` wires it through.
- `Location.getPictureId()` returns `null` when `isTooDarkToSee()` is
  true (Decision 4), mirroring the existing darkness gate on
  `getLongDescription()`.

## B. PICTURE action

Mirrors the `MessageAction`/`MessageActionData` triad exactly
(`server/action/MessageAction.java`,
`model/action/MessageActionData.java`,
`server/mapper/action/MessageActionMapper.java`,
`view/command/action/MessageActionEditor.java`):

- `PictureActionData extends ActionData { private String pictureId; }`
  — one-liner.
- `PictureAction extends AbstractAction`: `execute()` calls
  `gameContext.setCurrentPictureId(pictureId)` and returns an
  informational `CommandExecutionResult` (no text change required).
- `PictureActionMapper` (`@Service @AutoRegisterMapper`).
- `PictureActionEditor` (`@AutoRegisterActionEditor`,
  `ComboBox<String>` sourced from the adventure's picture names via
  `PictureService`), mirroring `MessageActionEditor`'s `ComboBox` sourced
  from `adventureData.getMessages()`.
- One new line in `ActionSelector.getAvailableActionTypes()`
  (`ActionSelector.java:62-79`):
  `new ActionTypeDescriptor("Picture", "Shows a picture for the rest of this turn", PictureActionData::new)`.

## C. Runtime trigger mechanism (final, revised)

`GameContext` gains `private String currentPictureId;`.

**Reset, once per sub-command.** `GameLoop.processCommand`'s per-sub-command
loop (`GameLoop.java:51-54`) already resets `currentPreposition`/
`currentAdverb`/`currentNoun2`/`currentAdjective2` fresh before each
sub-command dispatch, specifically to prevent staleness leaking across
sub-commands within one `submit()` call (the documented "location-staleness
trap"). `gameContext.setCurrentPictureId(null)` is added to that same reset
block. This is what makes "no picture" the default for every sub-command
unless one of the two triggers below, or a `PICTURE` action, sets it during
that sub-command's execution.

**Triggers, unified with the existing long/short description decision.**
`Location` already decides between long and short description text at
exactly the two moments a picture should show — first-visit arrival
(`getArrivalDescription()`, ternary on `timesVisited == 0`,
`Location.java:106-109`) and every explicit look (the built-in `describe`
Response always calls `getLongDescription()` unconditionally,
`CommandFactory.java:52-53`). Rather than duplicating that "is this a
long-description moment" check in a second place (the originally-designed
`ShowLocationPictureAction`), both call sites are changed to return the
picture alongside the text:

```java
// Location.java
public record LocationDescription(String text, String pictureId) {}

public LocationDescription getArrivalDescription() {
    boolean firstVisit = timesVisited == 0;
    String text = firstVisit ? getLongDescription() : getShortDescription();
    String pictureId = firstVisit ? getPictureId() : null;   // already darkness-gated
    return new LocationDescription(text, pictureId);
}

public LocationDescription getLookDescription() {
    return new LocationDescription(getLongDescription(), getPictureId());
}
```

- `MovePlayerAction.execute()` (`MovePlayerAction.java:26-38`) calls
  `destination.getArrivalDescription()`, and — critically — must do so
  **before** the `timesVisited` increment at line 32 (already true today,
  since the increment happens after the description is built); it then does
  `gameContext.setCurrentPictureId(result.pictureId())` alongside using
  `result.text()` for the existing message-building logic.
- `CommandFactory.setUpWorkflowCommands`'s built-in look/describe
  `GenericCommand`s (`CommandFactory.java:52-64`) are changed to call
  `getCurrentLocation().getLookDescription()` and set
  `gameContext.setCurrentPictureId(result.pictureId())` alongside using
  `result.text()`, replacing the current direct
  `getCurrentLocation().getLongDescription()` call. **No new Action class is
  introduced.**
- `PictureAction.execute()` is unaffected by the above — it overwrites
  `currentPictureId` directly and unconditionally, so it still works
  correctly even if an author overrides the built-in "describe" Response
  with their own commands (per the documented
  `RunArrivalProcessesAction` override-precedence gotcha,
  `RunArrivalProcessesAction.java:15-22`) as long as they attach a `PICTURE`
  action to their replacement.
- Any sub-command that is neither a first-visit arrival, a look, nor a
  `PICTURE` action (e.g. movement to an already-visited location, "take
  sword", "inventory") leaves `currentPictureId == null` for that
  sub-command — `AdventureRunView` hides the image region.
- Initial arrival (`AdventureRunView.beforeEnter()`,
  `AdventureRunView.java:116`) seeds `currentPictureId` the same way, via
  `getCurrentLocation().getArrivalDescription()`, since the starting
  location's first "visit" happens here rather than through
  `MovePlayerAction`.

## D. `PictureMenuView` / `PictureEditorView` / `PicturesMainLayout`

Cloned from the `location` package's CRUD triad
(`LocationsMenuView.java`, `LocationEditorView.java`,
`LocationsMainLayout.java`):

- `PicturesMainLayout` — drawer shell, same shape as `LocationsMainLayout`.
- `PictureMenuView` (`@Route(".../pictures")`, `@RolesAllowed("ROLE_AUTHOR")`)
  — searchable `Grid` of pictures (name + thumbnail `Image` bound to a
  `StreamResource` over the stored bytes + usage count), `Create`/`Edit`/
  `Delete`/`Back` actions, `ConfirmDialog` on delete.
- `PictureEditorView` (`.../pictures/edit`, `.../pictures/new`) —
  `TextField name`, and a Vaadin `Upload` component (**first use in this
  codebase** — no prior art to mirror for the upload UX itself, only for
  the surrounding CRUD scaffold) accepting image files, storing bytes +
  content-type directly into `PictureData.content`/`contentType` on save.
  `ResetBackSaveView` for the standard button row, matching
  `LocationEditorView`.
- **Delete guard**: new `PictureUsageTracker`
  (mirrors `LocationUsageTracker`) scans the adventure's
  `LocationData.pictureId` values and all `PictureActionData.pictureId`
  values for references before allowing deletion; if any exist, the
  `ConfirmDialog` is replaced with a blocking dialog listing each
  referencing location/action, matching Decision 3.
- New `RouteIds.PICTURE_ID` entry alongside the existing route-parameter
  enum constants (`RouteIds.java`).

## E. `AdventureRunView` — top-half display

`AdventureRunView.java:60-98` currently lays out a pure chat UI: a
`MessageList` inside a scrollable `VerticalLayout messageListContainer`,
`MessageInput`, and `backButton` — no top/graphic region exists today.

- New `Image pictureDisplay` component, backed by a `StreamResource` over
  the current picture's bytes, added above `chatLayout`
  (`AdventureRunView.java:97`'s `add(backButton, chatLayout)` becomes
  `add(backButton, pictureDisplay, chatLayout)` or equivalent container
  restructuring).
- Sizing: a `Div` wrapper around `pictureDisplay` with a fixed
  `min-height`/flex-basis of 50% of the view's height; `chatLayout` takes
  the remainder. `pictureDisplay.setVisible(false)` (collapsing the region
  entirely, not just hiding the image) when `currentPictureId` is `null`.
- After every `submit()` call (`AdventureRunView`'s `handleInput()`,
  `AdventureRunView.java:169-180`) and on `beforeEnter()`, the view reads
  `session.getGameContext().getCurrentPictureId()` and updates/hides
  `pictureDisplay` only if the value changed since the last render (avoid
  redundant `StreamResource` churn).

## F. Testing

One test class per layer, matching the codebase's existing convention:

- `PictureDataTest`, `PictureTest`, `PictureMapperTest` — data/runtime/mapper
  round-trip, mirroring `LocationDataTest`/`LocationTest`/`LocationMapperTest`.
- `PictureActionTest`, `PictureActionMapperTest` — mirrors
  `MessageActionMapperTest` (`src/test/java/.../MessageActionMapperTest.java`).
- `PictureMenuViewBrowserlessTest` / `PictureEditorViewBrowserlessTest` —
  CRUD + upload + delete-guard assertions, cloned from
  `LocationsMenuView`/`LocationEditorView`'s Browserless test pattern
  (mocked services, real POJO fixtures, `find()`/component testers).
- `LocationTest` extended: `getArrivalDescription()` returns the picture on
  first visit only, `null` on repeat visits, and `null` when
  `isTooDarkToSee()` regardless of visit count; `getLookDescription()`
  always returns the picture (subject to the darkness gate).
- `AdventureRunViewTest` (`AdventureRunViewTest.java`) extended:
  - picture shown on first-arrival `beforeEnter()`,
  - picture cleared on a move to an already-visited location,
  - picture shown again on an explicit "look" after being cleared,
  - `PICTURE` action overrides for that sub-command only, cleared on the
    following sub-command unless re-triggered,
  - suppressed picture (both arrival and look) when the location is dark,
  - a conjunction turn ("go north and look") — confirm the look's picture
    (or lack thereof) is what's visible at the end of the turn, since the
    per-sub-command reset means the last sub-command's outcome wins, same
    principle already verified for `runProcesses()` double-firing in
    `docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md`
    §Open questions #4.
- `GameLoopTest` (if one exists covering the per-sub-command reset loop) —
  add a case asserting `currentPictureId` resets between sub-commands.

## Open questions

1. **Upload size limit.** No limit specified by the user. Recommend
   reusing Vaadin `Upload`'s built-in `setMaxFileSize(...)` at a
   conservative default (e.g. 2MB) given the byte[]-in-Mongo storage
   decision (Decision 1) — large images stored this way risk the 16MB
   per-document Mongo cap in aggregate across many pictures, though each
   `PictureData` is its own document so the cap is per-picture, not
   per-adventure. To confirm with the user before implementation, or defer
   to a sensible default and revisit if it proves too restrictive.
2. **Accepted image formats.** Not specified. Recommend PNG/JPEG/GIF/WebP
   via `Upload`'s `setAcceptedFileTypes(...)`, matching what
   `Image`/`StreamResource` can render in-browser without additional
   conversion.
3. **Exact name of `LocationData`'s persistence access point** (repository/
   service interface) needs confirming during implementation so
   `PictureService` follows the same shape — not investigated in detail
   during design, called out as a to-verify item in Section A rather than
   guessed at.
