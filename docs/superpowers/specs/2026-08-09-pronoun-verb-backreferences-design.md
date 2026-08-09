# Parser back-references: "it" and inferred verbs

## Problem

The compound-command parser (`Parser.handle()` → `CommandSequence`, landed 2026-08-07) splits input on "and"/"then"/"." into independent sub-commands, but each sub-command is parsed in total isolation — there's no way to refer to an object or action mentioned earlier. Two natural constructions this blocks:

- **Noun back-reference**: `"take sword and shield and wear it"` — "it" should mean "the last thing mentioned" (here: shield).
- **Elided verb**: `"take sword and shield"` — the second clause ("shield") has no verb of its own today, so it falls through `GameLoop`'s existing bare-noun check ("I don't understand, please rephrase.") instead of being understood as "take shield".

Both are standard parser-IF conveniences and both are naturally expressed as *the parser remembering what was last said*.

## Design

### 1. New word type: `Word.Type.PRONOUN`, seeded word "it"

- Add `PRONOUN` to the `Word.Type` enum, alongside `VERB`/`NOUN`/`ADJECTIVE`/`CONJUNCTION`.
- Seed `"it"` as a built-in `PRONOUN` word in `MiniAdventure.createSpecialWords()` and `AdventureRunSessionFactory.registerBaseVerbs()`, next to the existing `"and"`/`"then"` seeding. Same treatment as those: no author CRUD, no vocabulary-menu visibility (that speculative feature was designed and then explicitly reverted in the prior session — out of scope here).
- Exclude `PRONOUN` from `WordEditorDialogue`'s type picker, same as `CONJUNCTION` is today.

### 2. Parser gains session-scoped memory: `lastVerb`, `lastNoun`, `lastAdjective`

One `Parser` instance already lives for the whole `AdventureRunSession` (constructed once in `start()`, reused every `submit()`), so these are just new instance fields — cross-turn persistence falls out of the existing lifecycle for free, with no new plumbing.

- **Update point — chunk close.** Today `Parser.handle()` has two call sites that do `commands.add(toDescription(currentSentence))` (one at each separator, one at end-of-input). Both become `commands.add(closeSentence(currentSentence))`, a new private method that:
  1. If the sentence has a noun but no verb, and `lastVerb` is non-empty, fills `verb = lastVerb` (**verb inference**).
  2. Builds the `GenericCommandDescription` via the existing `toDescription`.
  3. If the resulting verb is non-empty, `lastVerb` updates to it.
  4. If the resulting noun is non-empty, `lastNoun`/`lastAdjective` update *together* as a pair — "it" tracks the last mentioned *object* (noun+adjective), not a bare noun string, so `"take golden sword"` then `"drop it"` targets the golden sword specifically, not any sword.
- **Read point — pronoun resolution.** `populate()` gains a `PRONOUN` case: instead of literally setting `noun = "it"`, it sets `noun = lastNoun`, `adjective = lastAdjective`.
- State updates are driven purely by what was *parsed*, never by whether the resulting command succeeds at runtime — `Parser` has no visibility into execution outcomes (that lives entirely in `GameLoop`/`CommandExecutor`), so this is simplicity by construction, not extra logic: `"take sword"` (fails, no sword here) still leaves `lastNoun = "sword"`, so a later `"wear it"` still resolves to sword.

### 3. Unresolved "it" → specific message, whole line aborted

- If `PRONOUN` is scanned while `lastNoun` is still empty (no noun ever mentioned this session), `populate()` throws a new unchecked `UnresolvedReferenceException` carrying the message `"I don't know what 'it' refers to."` (built from the matched word's text, so it generalizes if more pronouns are added later).
- `GameLoop.processCommand()` catches it alongside the existing `QuitException`/`ReloadAdventureException` catches (must come before the generic `RuntimeException` catch), tells the message, returns `CommandOutcome.CONTINUE`.
- **Consequence, called out deliberately, not a bug to fix here**: `Parser.handle()` builds the *entire* `CommandSequence` before `GameLoop` executes any of it. So if "it" fails to resolve anywhere in a compound line, none of that line's sub-commands run — even ones earlier in the same line that would have parsed fine standalone. This only bites when "it" is used before any noun has ever been mentioned in the session (in practice, a first-command edge case). Approved as acceptable during design review.

## What this does not change

- Compound-command splitting itself (conjunction/period separators, fail-fast-on-first-failure execution) — untouched.
- The reverted author-facing built-in-word CRUD/visibility feature — not being resurrected as part of this.
- Adjective-only inference (e.g. inferring a *noun* from context) — not requested, not built. Only verb elision and noun back-reference via "it" are in scope.

## Testing plan

- `ParserTest`: worked example (`"take sword and shield and wear it"` → 3 commands); verb inference within one line; `lastVerb`/`lastNoun`/`lastAdjective` persistence across separate `handle()` calls; adjective+noun paired resolution; explicit verb not overridden by inference; `UnresolvedReferenceException` when "it" has no antecedent.
- `GameLoopTest`: unresolved-pronoun message end-to-end (`CONTINUE`, not `ERROR`); a failed sub-command still updates pronoun state for a later command in the same session.
- `WordEditorDialogueTest`: `PRONOUN` excluded from the type picker, mirroring the existing `CONJUNCTION` exclusion test.
- `MiniAdventureTest`/`AdventureRunSessionFactoryTest` (whichever already covers `createSpecialWords`/`registerBaseVerbs`): assert "it" is seeded as `PRONOUN`, mirroring existing "and"/"then" coverage.
