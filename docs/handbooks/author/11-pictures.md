# 11. Pictures

A **Picture** is an image you upload to your adventure so players see it next
to the text. During play, the current picture appears in a panel above the
transcript; when there's no picture to show, the panel simply isn't there.

You can attach a picture to a location (its **default picture**), or show any
picture at a chosen moment with a **Picture** action. Both are covered below,
after the screens where you manage the images themselves.

## The pictures list

**Pictures** on the [Adventure Editor](04-the-adventure-editor.md) opens a
list of your adventure's pictures — a running **Pictures: N** count, plus
**Edit Picture** (enabled once you select a row), **Create Picture**, and
**Back** on the left; a **Find picture** search field (filters by name) and the
grid itself on the right. The grid shows a small **Preview** of each image, its
**Name**, and **Used** — how many places currently show it. With no pictures
yet it reads *"No pictures found. Upload some to bring locations to life."*

Double-click a row to edit it, or right-click for **Edit**, **Find Usage**, and
**Delete**.

**Delete** is refused while the picture is still in use: instead of the
confirmation dialog you're shown the list of places that use it, so you can
clear them first. A picture counts as used when it is a location's default
picture, or when a **Picture** action shows it anywhere — in a location's
commands or exits, in an item's commands (whether the item lies in a location
or is carried in the player's pocket), or in any of the three
[workflow](09-workflow.md) lists. An unused picture asks for confirmation and
is then removed for good.

## The picture editor

| Field | Notes |
|-------|-------|
| **Name** | Required. A label for you — it's what you pick in the **Default Picture** and **Picture** drop-downs. It doesn't have to be unique. |
| **Upload** | One image: **PNG, JPEG or WebP**, **smaller than 2 MB**. A preview appears as soon as the upload succeeds. |

Adventure Builder looks at the file's actual contents, not its name. A file
that merely *ends* in `.png` but isn't really a PNG, JPEG or WebP image is
refused with *"The uploaded file is not a recognized PNG, JPEG, or WebP
image."*, and one that's too large with *"Pictures must be less than 2MB in
size"*.

The usual [Cancel / Reset / Back / Save bar](05-locations-and-exits.md#the-standard-editor-button-bar)
applies. **Save** stays disabled until the picture has a name **and** an image,
and something has changed — so you can't save a picture without an image. A
freshly uploaded image is only *staged* until you press **Save**; **Reset**
throws it away. To replace the image of an existing picture, open it and upload
a new file. Leaving with an unsaved name or a staged upload asks for
confirmation first.

## Showing a picture

There are two ways, and they work together.

### A location's default picture

The [location editor](05-locations-and-exits.md#the-location-editor) has an
optional **Default Picture** drop-down listing your adventure's pictures by
name. A location with a default picture shows it to the player like this:

| When | What happens to the picture |
|------|-----------------------------|
| The player arrives at the location **for the first time** | The default picture is shown. |
| The player arrives at it **again** | No picture is shown, and whatever was showing is **cleared** — the panel disappears. |
| The player types `look` (or `l`, `desc`) | The default picture is shown, **every** time. |

> **Note:** A location's picture is only shown if the player can see. If the
> location is too dark, there's no picture. "Too dark" means the location's
> **Lighting (Lumen)** plus the light of every lit item — lying in the room *or*
> carried in the pocket — adds up to less than **10**. A location's default
> lighting is 50, so this only affects places you've deliberately made dark; a
> lit lamp in the player's pocket brings the picture back.

### The Picture action

A **Picture** action ("Show a picture to the player until the next move, look,
or picture action") shows one picture of your choice at that moment. Add it to
any command's list of actions like any other action (see
[Chapter 8](08-commands-actions-and-conditions.md)) — in a location or item
command, an exit, or a [Workflow](09-workflow.md) Process, Arrival Process or
Response. Pick the picture from the required **Picture** drop-down; in the
command's action list it appears as `PICTURE <name>`.

The picture stays up until the player moves to another location, looks, or a
different Picture action runs.

> **Tip: show a picture on every arrival, not just the first.** Add an
> **Arrival Process** ([Workflow I](09-workflow.md#arrival-processes-run-when-a-location-is-described))
> with a **Player At** precondition for the location and a **Picture** action.
> Arrival Processes run *after* the location's own picture has been set, so
> yours wins — and it comes back on every visit.

## What players see

The picture panel sits above the transcript on the
[play screen](12-testing-and-running.md#the-play-screen). It scales the image
to fit — at most 640 × 480 pixels — and updates after every command. Moving,
looking and Picture actions are the only things that change it; a command that
just answers a question leaves the current picture alone.

## What's next

You've now covered every editing screen.
[Chapter 12](12-testing-and-running.md) covers actually playing what you've
built.
