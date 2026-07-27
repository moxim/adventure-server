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

## Messages you don't have to create

Adventure Builder ships a handful of built-in fallback messages for common
situations — trying to wear something that can't be worn, remove something
you're not wearing, and similar — so those situations have sensible default
text even in an adventure where you haven't written anything for them
yourself. These built-ins don't appear in the Messages list above and
aren't editable there; they're just a safety net so a fresh adventure never
shows a blank response to a reasonable action.

## What's next

You've now covered every editing screen. [Chapter 11](11-testing-and-running.md)
covers actually playing what you've built.
