# 3. Your Adventures

This is the page titled **"Your World Of Adventures"** — reached from the
Author Dashboard's **Adventures** button, or the **Dashboard** entry in any
drawer. It lists every adventure you have access to. As a plain Author
that's the adventures you own; an Admin sees every adventure in the system.

## The layout

- On the left: a **Create Adventure** button, an **Import Adventure** button
  (see [Importing an adventure](#importing-an-adventure)), and a **Run
  Adventure** button.
- On the right: a grid with one column, **Title**, listing your adventures,
  above a small hint: *"Double-click a row to edit"*

If the grid is empty, it says so plainly: *"Create some adventures."*

## Creating an adventure

Click **Create Adventure**. This takes you straight to a blank
[Adventure Editor](04-the-adventure-editor.md) — nothing is saved yet, so
feel free to back out if you change your mind.

## Opening an adventure to edit it

Two ways, both land on the [Adventure Editor](04-the-adventure-editor.md):

- **Double-click** a row.
- **Right-click** a row for a context menu with an **Edit** entry (the menu
  also offers **Duplicate**, **Export** and **Delete**, covered below).

The same right-click menu also shows a line with that adventure's **Notes**
text — the same free-text scratchpad from the
[Adventure Editor](04-the-adventure-editor.md) — as a quick peek without
opening the editor. Clicking that line doesn't do anything; it's
informational only.

## Duplicating an adventure

Right-click a row → **Duplicate**. This makes a complete, independent copy
of the adventure: every location, exit, item, picture, word, and message
comes along, and the copy is titled like the original, with *(copy)* added, so *The Lost Key* becomes *The Lost Key (copy)*. It appears
in the grid straight away, and you are its author.

The copy shares nothing with the original — edit or delete either one and
the other is untouched. Players assigned to the original are **not**
carried over to the copy. You can only duplicate adventures you own (an
Admin can duplicate any adventure, and then owns the copy).

## Exporting an adventure

Right-click a row → **Export**. Your browser downloads the whole adventure as
one file, named after its title: an adventure called *The Lost Key* arrives as
`The-Lost-Key.adventure.json` (anything that isn't a letter or a digit
becomes a dash).

The file holds everything that belongs to the adventure — every location,
exit, item, picture, word, message, and workflow rule. It does **not** hold
the people attached to the adventure (its author and its players) or any
games players have saved; those belong to the accounts on the installation
you exported from.

Exporting changes nothing in your adventure, so use it freely:

- to move an adventure to another installation of Adventure Builder,
- to keep a backup before a big change,
- to hand an adventure to a colleague, who imports it (below).

You can export adventures you own (an Admin can export any adventure). If
something goes wrong you see *"Could not export adventure 'The Lost Key'."* (with your adventure's title)
and no file is downloaded.

**Note:** The file is plain text, but it isn't meant to be edited by hand.
Pictures are stored inside it as encoded text, and a file that has been
changed — even slightly — may be refused on import.

## Importing an adventure

Click **Import Adventure** on the left of the list. An **Import Adventure**
dialog opens.

1. Choose an exported `.json` file in the upload area (or drag it in). It
   takes one file at a time.
2. When the upload has finished, the **Import** button becomes available.
   Click it. (**Cancel** closes the dialog without importing anything.)
3. The dialog closes, the adventure appears in the list, and a message says
   *"Adventure 'The Lost Key' imported."* (with the adventure's title)

Things worth knowing:

- **An import always creates a new adventure.** It never overwrites or
  merges into an existing one, even if the title is the same. Importing the
  same file twice gives you two adventures with the same title, so rename one
  in the [Adventure Editor](04-the-adventure-editor.md) if you need to tell
  them apart. (Unlike **Duplicate**, the import does not add *(copy)* to the
  title.)
- **You become its author.** Players who were assigned to the original are
  not carried over.
- **The file is checked completely before anything is saved.** If anything is
  wrong, nothing is imported, you see a red message, and the dialog stays
  open so you can pick another file or cancel.
- **Files have a size limit,** 50 MB by default (pictures make files big). The
  upload area refuses a bigger file. A server administrator can change the
  limit.
- **Exported with a different version of Adventure Builder?** The import still
  goes ahead, but the message adds *"The file was written by another version
  of the builder - please check the adventure."* Open it and play it through
  before relying on it.

### If an import is refused

The red message starts with *"Could not import the adventure:"* followed by
the reason:

| The message says | What it means |
|------------------|---------------|
| *This is not an adventure file.* | The file wasn't exported by Adventure Builder, or it is damaged. |
| *The file is empty.* | There was nothing in the file. |
| *The file is not valid JSON.* | The file is cut off or has been edited. Export it again. |
| *The file is larger than the allowed 50 MB.* | Over the size limit. |
| *The file uses format version N, but this builder reads up to version M. Please update the builder.* | The file comes from a newer Adventure Builder than the one you are using. |
| *The file must contain exactly one adventure.* | An export holds a single adventure; this file holds none or several. |
| *The file is incomplete: it refers to documents it does not contain (…).* | Part of the adventure is missing from the file. Export it again from the source. |
| *The file contains data this builder cannot read.* | The file is well-formed, but something in it (a font, say) isn't one this version understands. Nothing is left behind. |
| *The file contains a damaged or duplicated document…*, *…a reference that is not valid*, *…a field name '…', which adventures do not use*, or *…data for '…', which adventures do not use* | The file was changed after it was exported, or wasn't made by Adventure Builder. |

A plain *"Could not import the adventure."* with no reason means something
unexpected went wrong; nothing was imported. Try once more, and if it
persists, ask an Admin to look at the server log.

## Deleting an adventure

Right-click a row → **Delete**.  
A **Delete Adventure** dialog asks _"Are you sure you want to delete 'The Lost Key'? This cannot be undone."_ (with your adventure's title)  
Click **Cancel** to keep the adventure, or **Delete** to confirm — the row then disappears from the grid and the adventure (and everything inside it: every location, item, word, and message) is removed.

**Note:** Deleting is permanent. Once you confirm there's no undo.


## Running an adventure

Single-click a row to select it — this enables the **Run Adventure**
button — then click the button. (Double-clicking a row opens the *editor*
instead, so don't use double-click if you just want to play.)
**Run
Adventure** launches the same interactive play screen covered in
[Chapter 12: Testing & Running](12-testing-and-running.md).

## Ownership

You'll only ever see adventures you author here. If a colleague's adventure
should be yours to edit too, that's an Admin's call — they assign one
Author per adventure via user management, which is outside the Author role.

## What's next

[Chapter 4](04-the-adventure-editor.md) covers everything on the Adventure
Editor screen you land on after **Create Adventure** or **Edit**.
