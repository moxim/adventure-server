# 10. Messages

A **Message** is a reusable piece of text with an id, printed by a
**Message** action wherever you reference that id. Useful for anything
you'll want to say more than once, or want to tweak in one place later
(flavor text, recurring hints, an ending).

**Manage Messages** on the Adventure Editor opens the list: **Create
Message**, **Back**, and a running **Total messages: N** count on the
left; a search field (filters by id or text) and the grid on the right,
with columns **Message ID**, **Message Text**, **Length**, and **Used**
(how many commands reference it).

Right-click a row for a small menu: a preview of the full message text,
then **Edit**, **Find Usage**, **Duplicate**, and **Delete**. Deleting a
message that's still referenced is refused — Adventure Builder tells you
how many places still use it, so clean those up first.

## The message editor

| Field | Notes |
|-------|-------|
| **Message ID** | Alphanumeric characters and underscores only (the field's own helper text says so). This is what a Message action references. |
| **Message Text** | The body. |
| **Preview** | A live preview of the text as you type it. |
| **Usage** | Lists every place this message is referenced, under a *"Used in N location(s):"* heading — or, if nothing references it yet, *"This message is not currently used anywhere in the adventure."* A quick way to spot dead messages before you clean them up. (On a brand-new, unsaved message, this just says usage info will be available after saving.) |

## Editing the built-in system messages

Separately from the messages *you* write, Adventure Builder's engine has its
own fixed set of built-in lines — *"I can't do that."*, *"OK."*, *"I already
have the %s."*, *"What now?"*, and dozens more — that it uses for common
situations so a fresh adventure never shows a blank response to a reasonable
action.

You can't add or remove these, but you **can reword or translate them**, per
adventure, from the Adventure Editor's **Manage System Messages** button.
That opens a screen headed *"System Messages for {your title}"* — a grid of
every built-in message with its **Key** and **Current Text**. Double-click a
row to edit it. The dialog shows:

- the **original English** text,
- a short note on what the message is for and where the engine uses it,
- an editable **Text** box.

| Rule | Why |
|------|-----|
| You can only **edit** — no create, delete, or rename. | The catalog is fixed; the engine looks these up by key. |
| Your edits are **per adventure**. | Two adventures can run in different languages without their wording leaking between them. |
| The new text must keep the **same placeholders** (`%s` and the like) as the original. | The engine fills those in at runtime (e.g. the item's name); dropping one would break the message. Save is refused with an error if they don't match. |
| An unedited message has no stored row at all. | It just reads as the default until the first time you change it — so leaving most of them alone costs nothing.|

## What's next

You've now covered every editing screen. [Chapter 11](11-testing-and-running.md)
covers actually playing what you've built.
