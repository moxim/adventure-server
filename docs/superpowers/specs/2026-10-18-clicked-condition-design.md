# CLICKED condition — design

Status: approved by the author. Date: 2026-10-18.

## Intent

Today a player can only act by typing. This adds a second input stream, the mouse:
when a player clicks into the picture that the running adventure shows, the author's
**Processes** (Workflow II, "Processes" table) are scanned, and a Process whose
**CLICKED(x, y)** precondition matches the clicked spot performs its actions. Typical use:
the picture shows a door, the author adds a Process "CLICKED 3, 4" with a MovePlayer
action, and clicking the door takes the player through it.

The author places CLICKED like any other precondition, with two arguments, x and y.

## Goals

- A new precondition `CLICKED(x, y)`, authored with the existing precondition editor.
- Clicks reach the engine through the run view's picture; clicking runs the Processes
  table and nothing else (no verb parsing, no location/pocket commands, no Responses).
- The picture is divided exactly like the world map in `LocationMapView`: a fixed
  **10 × 10 grid**, cells numbered from 0, **x = column (left to right), y = row (top to
  bottom)**, so valid values are 0–9. The grid covers the picture itself, not the space
  around it.
- Works with the picture that is currently displayed (the location's/picture action's
  picture); no picture shown means nothing can be clicked.
- Clicking a spot where no CLICKED Process applies answers with system message **SM6**
  ("I was not able to understand any of that. Please try again."), followed, like after
  any turn, by the usual prompt (one of SM2–SM5, steered by the PROMPT variable).

## Non-goals

- Not offered for Responses, Arrival Processes or commands on locations/items/exits: a
  click never dispatches a verb, so there CLICKED could never be true.
- No free-form hotspots (rectangles, polygons) and no other grid size; only the
  10 × 10 grid.
- No hover, right-click, drag or touch gestures; one plain click.
- No change to what typed input does.

## Anti-goals

- A typed turn must never see CLICKED as true (a click must not "stick" and fire on the
  next typed command).
- A click must never produce the "which verb?" style errors typed input produces, apart
  from the SM6 miss message above.

## Behaviour

1. The player clicks the picture in the run view. The view works out the cell (x, y) from
   where inside the *picture* the click landed (not the surrounding box: if the picture is
   shown smaller than its box, clicks in the margins are ignored) and hands it to the
   session as a click.
2. The session runs the Processes table with the click recorded in `GameContext` for the
   duration of that run. All Processes are tried, as after a typed command, in the same
   order; a Process containing `CLICKED(x, y)` passes that precondition only when the
   recorded click is that cell. Other preconditions and actions behave as always. A
   Process with no CLICKED condition also runs on a click, exactly as it does after every
   typed command.
3. For the duration of the click run, the typed-input fields in `GameContext` (verb,
   adjective, noun, preposition, adverb, noun 2, adjective 2) are reset to empty, so that
   leftovers of the previous typed command cannot satisfy conditions on a click; only
   CLICKED, location, state and similar conditions can match. The click is cleared when
   the run ends, hit or miss. *My call.*
4. Output: the Processes' messages are printed, then the prompt. A click is a **hit** when
   at least one command in some Process chain has a **positive CLICKED condition** (not
   wrapped in NOT, at any nesting depth) and that command's preconditions all passed so its
   actions ran — regardless of whether it printed any text. Otherwise it is a **miss** and
   SM6 is printed first (so a click on an empty spot is visibly "not understood", and a
   click whose CLICKED Process has other guards that fail counts as a miss too).
   `NOT_CLICKED` never makes a hit. The engine needs a way to know that a command's actions
   ran (an "applied" signal), since today only the message text comes back. *My call* (all
   of this paragraph).
5. Every click counts as a turn, hit or miss: the TURNS variable goes up by one before the
   Processes run, as for a typed phrase with a verb. *The author's decision* (revised after
   first deciding that a miss does not count).
6. Game over / superseded sessions behave as for typed input (the click is ignored or
   answered with the same "game over"/"ended elsewhere" result).

## Authoring

- The precondition selector lists "Clicked (matches a click on the picture)", shown in
  the Processes editor only (*my call*: scoping the selector by list type is wanted, since
  elsewhere it can never be true).
- Editor: two integer fields, "X (column, 0–9)" and "Y (row, 0–9)", both required,
  validated 0–9, like the other single-purpose condition editors.
- `PreconditionActionFormatter` shows it as `CLICKED x, y` (e.g. `CLICKED 3, 4`);
  `NOT_CLICKED 3, 4` works through the existing NOT wrapper.
- A Process may combine CLICKED with other preconditions (e.g. `WORN gloves`).

## Constraints and conventions

- All work in the `server` module, following the existing layering used by
  `PrepositionCondition`/`ChanceCondition`: data class (`model/condition`), runtime
  condition (`server/condition`), mapper (`server/mapper/condition`, auto-registered),
  editor + selector entry (`view/command/condition`), formatter line, game-context state.
- The click state lives in `GameContext`, cleared in a `finally`, like the per-sub-command
  preposition/adverb state; it must not leak into saved games (check
  `GameStateSnapshotter`).
- Tests per `AGENTS-testing.md`; docs: update `docs/specs` (UI + runtime engine),
  author handbook (conditions chapter, appendix A, workflow chapter) and `CHANGELOG.md`.
- JDK 25, run Maven from `server/`.

## Left to the builder

- How exactly the click is read in the browser (Vaadin `ClickEvent` relative coordinates
  or a small client-side script) and how the picture's rendered area is determined when
  it is letterboxed; the result must match the world map's cell numbering.
- How the session entry point is named, and where exactly the TURNS increment happens so
  that a hit behaves like an understood typed command (e.g. what a Turns action in the same
  Process shows).
- How to scope the selector to the Processes list.
