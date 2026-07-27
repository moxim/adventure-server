# 5. Locations & Exits

Locations are the rooms of your world. Exits (you'll also see the older
term *Direction* in a couple of places) are the verbs that move a player
from one location to another — `north`, `enter cave`, `climb tree`,
whatever you choose.

## The standard editor button bar

Before diving in: most editors from here on (Locations, Items, Exits,
Commands, Messages — everything except the Adventure Editor itself) share
one consistent four-button bar, always in this order:

| Button | Behaviour |
|--------|-----------|
| **Cancel** | Discard unsaved changes and go back, in one click. |
| **Reset** | Reload the form from its last saved state. Stays on the page. |
| **Back** | Go back to the parent list. If you have unsaved changes, you'll be asked to confirm before leaving. |
| **Save** | Validate and save. Only enabled once there are changes and they're all valid. |

This handbook won't re-describe this bar in every chapter — assume it's
there unless a chapter says otherwise.

## The locations list

**Manage Locations** (from the Adventure Editor) opens a grid of your
adventure's locations — a running **Locations: N** count, plus **Edit
Location** (enabled once you select a row), **Create Location**, and
**Back** on the left; a **Find location** search field (filters by id,
noun, or short description) and the grid itself on the right, showing each
location's description, **Lumen** (light level), and **Used** (how many
exits currently lead to it).

Right-click a row for **Edit**, **Select as start** (see below),
**Find Usage**, and **Delete**. Deleting a location that's still in use
(referenced by an exit or a command) is refused, with a notification
telling you how many references to clear first; otherwise you'll get a
confirmation dialog before it's removed.

### Choosing the starting location

An **Entry location** picker also lives on this screen — this is how you
set which location is shown to a player the moment they start (or test)
your adventure. Whatever you pick here is what shows up as **Start
Location** back on the Adventure Editor. The right-click **Select as
start** context-menu entry does the same thing.

## The location editor

| Field | Notes |
|-------|-------|
| **Adjective** | Optional, from your vocabulary. |
| **Noun** | Required, from your vocabulary — pick this before typing descriptions. |
| **Short description** | If you leave this blank, it's derived from the Adjective/Noun you picked. |
| **Long description** | Same auto-derivation if left blank. This is what a player reads when they arrive or `look`. |
| **Lighting (Lumen)** | An integer, 0–100. |
| **Number of exits** | Read-only. |
| **Location ID** / **Adventure ID** | Read-only identifiers. |

Three buttons take you further into this one location's content:

- **Manage Commands** → the [Command editor](08-commands-actions-and-conditions.md) scoped to this location.
- **Manage Items** → the [item list](06-items.md) scoped to this location.
- **Manage Exits** → below.

## Exits

**Manage Exits** opens a grid of this location's exits, each row showing
its **Command** (the verb/adjective/noun that triggers it) and its
**Destination**, alongside a **Create Exit** button and a **Find exit**
search field (filters by id, noun, or short description). Double-click a
row, or right-click it for **Edit**, to open the exit editor; right-click
→ **Delete** removes it immediately, with no confirmation and no check for
what else might reference it — a second spot in the app (alongside
[deleting an adventure](03-your-adventures.md#deleting-an-adventure)) where
there's no safety net.

The exit editor:

| Field | Notes |
|-------|-------|
| **Verb** | Required — the word that triggers this exit (e.g. *north*). |
| **Adjective** / **Noun** | Optional, for exits like `enter dark cave`. |
| Destination picker | A grid of locations to pick the destination from. Picking the *current* location as its own destination triggers a confirmation warning. |
| **Short description** / **Long description** | Text for this exit. |
| **Direction ID** / **Location ID** / **Adventure ID** | Read-only identifiers. |

## Deleting a location

If any exit or command still targets a location, deleting it is refused —
clean up what points at it first.

## The World map

The drawer for this section (icon: a stack of maps) includes a **"The
World"** link to a map view.

> **Note:** Today, this map is an early placeholder — a static illustration
> with a hardcoded grid you can click for a location-name popup, not a live
> view of your actual locations and how they connect. Don't rely on it to
> visualize your adventure's layout yet; the locations grid above is the
> accurate source of truth.

## What's next

[Chapter 6: Items](06-items.md) covers the things players find inside your
locations.
