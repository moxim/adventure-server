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
5. **Runtime display is persistent, not reset on every sub-command.** A
   picture, once shown, stays on screen through unrelated commands (e.g.
   "take sword", "inventory") and only changes when one of the three
   triggers fires again: a move (to the new location's picture, or to no
   picture if not a first visit — "replaced even with nothing"), an explicit
   look (always re-shows the current location's picture), or a `PICTURE`
   action. A first draft of this design reset `currentPictureId` to `null`
   once per sub-command and required one of the three triggers to
   repopulate it every time — rejected because it cleared the picture after
   any intervening non-triggering command, which is not the desired
   behavior. See Section C.
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

`GameContext` gains `private String currentPictureId;`, initially `null`.
**No reset hook is added to `GameLoop`** — unlike `currentPreposition`/
`currentAdverb`/`currentNoun2`/`currentAdjective2` (reset every sub-command
to avoid the documented "location-staleness trap",
`GameLoop.java:51-54`), `currentPictureId` is deliberately *not* reset
there. It only ever changes via one of the three triggers below; any other
sub-command (e.g. "take sword", "inventory") leaves it untouched, so
whatever picture was last shown remains on screen.

A useful side effect: a **failed** move (e.g. "go north" with no exit that
way) never reaches the code that sets `currentPictureId`, so a failed move
correctly leaves the current picture in place too — only a *successful*
move counts as "the move that replaces the picture."

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
- A move to an **already-visited** location still counts as a trigger (it
  calls `getArrivalDescription()`, which returns `pictureId == null` for a
  repeat visit) — so `currentPictureId` is explicitly cleared, matching
  "replaced even with nothing." Any *other* sub-command that is neither a
  move, a look, nor a `PICTURE` action (e.g. "take sword", "inventory")
  leaves `currentPictureId` completely untouched — the previously shown
  picture (or lack of one) persists.
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
  the surrounding CRUD scaffold), constrained to PNG/WebP/JPEG and 2MB max
  (see "Upload constraints" below), storing bytes + content-type directly
  into `PictureData.content`/`contentType` on save. `ResetBackSaveView` for
  the standard button row, matching `LocationEditorView`.
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
  - `PICTURE` action shows its picture, which then **persists** through a
    subsequent unrelated command (e.g. "take sword") — asserts no reset
    hook exists, not just that the trigger works,
  - a failed move (invalid direction) leaves the previously shown picture
    unchanged,
  - suppressed picture (both arrival and look) when the location is dark,
  - a conjunction turn ("go north and look") — confirm the look's picture
    is what's visible at the end of the turn (last trigger wins), same
    principle already verified for `runProcesses()` double-firing in
    `docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md`
    §Open questions #4.

## Upload constraints (resolved)

- **Accepted formats**: PNG, WebP, and JPEG only — via `Upload.setAcceptedFileTypes("image/png", "image/webp", "image/jpeg")`. `PictureEditorView` rejects anything else at the component level; `PictureData.contentType` is expected to always be one of these three.
- **Maximum size**: 2MB per picture, via `Upload.setMaxFileSize(2 * 1024 * 1024)`. Comfortably clear of the 16MB Mongo per-document cap for a single `PictureData` document.

## Open questions

1. **Exact name of `LocationData`'s persistence access point** (repository/
   service interface) needs confirming during implementation so
   `PictureService` follows the same shape — not investigated in detail
   during design, called out as a to-verify item in Section A rather than
   guessed at.
