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
| **Back** | Go back to the parent list. If you have unsaved changes, you'll be asked to confirm before leaving. Without unsaved changes it steps back in your browser's history, so going in and out of an editor does not pile up entries behind your browser's Back button. |
| **Save** | Validate and save. Only enabled once there are changes and they're all valid. |

This handbook won't re-describe this bar in every chapter — assume it's
there unless a chapter says otherwise.

## The locations list

**Locations** (on the Adventure Editor) opens a grid of your
adventure's locations — a running **Locations: N** count, plus **Edit
Location** (enabled once you select a row), **Create Location**, and
**Back** on the left; a **Find location** search field (filters by id,
noun, or short description) and the grid itself on the right, showing each
location's description, **Lumen** (light level), **Used** (how many
exits currently lead to it), and **Commands** (how many commands you have
defined in the location).

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
| **Lighting (Lumen)** | An integer, 0–100. Also decides whether a player can see this location's picture — see [Chapter 11](11-pictures.md#a-locations-default-picture). |
| **Default Picture** | Optional. One of your adventure's [pictures](11-pictures.md), shown to the player when they arrive for the first time and whenever they `look`. |
| **Number of exits** | Read-only. |
| **Number of commands** | Read-only: how many commands this location has (the rows you see under **Manage Commands**). |
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
→ **Delete** asks you to confirm in a **Delete Exit** dialog before removing
it, like every other delete in the app (see
[deleting an adventure](03-your-adventures.md#deleting-an-adventure)). Unlike
locations, items and words, it does not check what else might reference the
exit.

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

The **The World** button on the locations list, just above **Edit
Location**, opens a map of your adventure. The map is the picture you chose
as the adventure's **World Map** in the Adventure Editor, with a 20 × 20
grid of clickable cells over it. (Without a chosen picture you see a hint
and a button to the Adventure Editor.) The **Back** button below the picture
returns to the locations list.

Locations are placed on the grid. A cell that holds a location is drawn
darker, and hovering over it shows the location's description. The cell of
the adventure's **start location** (see the *Entry location* picker above)
shows a **yellow star**. A mode switch above the picture decides what a click
on a cell does:

| Mode | Click on… | What happens |
|------|-----------|--------------|
| **Place** (the default) | an empty cell | A dialog with two tabs. **Unplaced locations** lists the locations that are not on the map yet: pick one and press **Place**. **New location** makes a brand-new location right on that cell: choose its **Noun** from your vocabulary (required), optionally an **Adjective** and a **Short description**, and press **Place**. Everything else (long description, light, picture, commands, exits) you edit later in the location editor. If every location is already on the map, only the new-location form is shown. |
| **Place** | a cell with a location | The **Replace** dialog: pick an unplaced location that takes the cell. Exits that led to the old location now lead to the new one, and you tick which of the old location's own exits move to the new one. |
| **Connect** | one location, then another | A dialog lists the exits that already lead from the first to the second and lets you add one, optionally with the opposite exit back. You choose the words from the adventure's vocabulary. |
| **Remove** | a cell with a location | A dialog takes the location off the map. The location itself stays in the adventure and can be placed again. |

When you remove a location you also decide what happens to its exits. The
dialog lists **the exits leading away from it** and **the exits leading to
it** (those that start in other locations), each with a checkbox. Nothing is
ticked at first: only the exits you tick are deleted, the others stay and
still lead to the location. **Select all** ticks a whole list. If you remove
the start location, it stays the adventure's start location.

## What's next

[Chapter 6: Items](06-items.md) covers the things players find inside your
locations.
