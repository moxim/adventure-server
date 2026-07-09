# Deep-Linkable Menu & Editor Views Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 13 Vaadin views (8 menu views + 5 child editors) that render empty on a fresh URL load, by making each view's `beforeEnter` resolve everything it needs from route parameters via a new shared `AdventureRouteResolver`, instead of relying on the caller to hand data over by reference after navigation.

**Architecture:** A new static utility, `AdventureRouteResolver`, wraps `AdventureAccessService.findAdventureById(id, user)` plus in-memory nested lookups (location/item/direction/command/message) behind `Optional`-returning methods that show a not-found notification on failure. Each of the 13 views' `beforeEnter` calls the resolver methods it needs and forwards to the nearest still-valid parent view on failure. A final cleanup task removes the now-dead `setData()`/`setAdventureData()` handoff methods and simplifies every caller's navigation call site.

**Tech Stack:** Vaadin Flow 25.2, Spring Boot, Spring Security, MongoDB (Spring Data, `@DBRef`), JUnit 5, Mockito, AssertJ, `com.vaadin.browserless.BrowserlessTest`.

## Global Constraints

These apply to every task below; task text does not repeat them.

1. **Resolver dependency, not a parameter.** `AdventureRouteResolver.resolveAdventure(event, accessService)` calls `ViewSupporter.getCurrentUser()` internally. Callers never pass a `UserData`.
2. **Add `AdventureAccessService`, never remove an existing service.** Every one of the 13 views' constructors currently injects `AdventureService` (some also `ItemService`/`MessageService`). None currently injects `AdventureAccessService`, even though it's a real, already-registered Spring bean (`AdventureEditorView` already uses it). Add it as a new constructor parameter; keep every existing parameter as-is, even ones that are already unused today (e.g. `ItemsMenuView`/`AllItemsMenuView`'s unused `ItemService anItemService`) — that's pre-existing and out of scope for this fix.
3. **`beforeEnter` calls the view's existing `setData()`/`setAdventureData()` method directly — never deletes or inlines it.** Tasks 2–14 add/rewrite `beforeEnter` to resolve data and pass it to the SAME population method the caller used to call (e.g. `setAdventureData(resolvedAdventure.get())`) — that method's body is untouched (Global Constraint §10). Tasks 2–14 must NOT touch any caller's `.ifPresent(editor -> editor.setXxx(...))` call site — that's Task 15 only, done as one coordinated sweep once every view self-populates. Until Task 15, an in-app navigation populates a target view twice (once via the new `beforeEnter`, once via the caller's still-present `.ifPresent(...)` handoff) — this is expected, not a bug: nothing exercises the live running app mid-plan, and every task's own tests drive the view exclusively through `beforeEnter` via a mocked `BeforeEnterEvent`, never through the live in-app handoff. Task 15 removes the caller-side handoff and downgrades each population method from `public` to `private`, since by then it's only ever called internally by its own `beforeEnter`.
4. **Tests must never call `setData()`/`setAdventureData()` directly.** Every new/updated test drives the view only through `beforeEnter` with a constructed `BeforeEnterEvent` (real or mocked — see each task). This is what lets Task 15 downgrade those methods to `private` later without breaking any test.
5. **Happy-path tests must prove route-parameter extraction, not just stub around it.** Stub `AdventureAccessService.findAdventureById(...)` (and any nested resolution) with the *exact* id used in the mocked event's `RouteParameters`, and assert the view actually used the resolved data (grid contents, counter text, etc.) — not `any()`/loose stubbing, which would pass even if `beforeEnter` never read the route parameter at all (the exact bug being fixed).
6. **Every view gets a new, dedicated `<ViewName>RoutingTest.java` file — existing test files are never read or modified by Tasks 2–14.** This codebase already has precedent for multiple purpose-named test classes per view (`DirectionEditorViewTest`, `DirectionEditorViewNavigationTest`, `DirectionEditorViewEdgeCasesTest`, `DirectionEditorViewBinderTest`, `DirectionEditorViewDataIntegrityTest` all coexist today). Each task below follows that pattern: a brand-new file, named `<ViewName>RoutingTest.java` (for the 2 views with no prior test file at all — `LocationsMenuView`, `DirectionsMenuView` — the plain name `<ViewName>Test.java` is used instead, since there's no collision to avoid), extending `com.vaadin.browserless.BrowserlessTest`, covering BOTH the happy path (route params → correct population, proving extraction per Global Constraint §5) AND the not-found path (`event.forwardTo(...)` called with the correct target, notification shown). Because every one of these files is new, there's no pre-existing plain-Mockito convention to preserve, so `BrowserlessTest` (needed for the not-found path's `Notification.show(...)` to run) is used uniformly across all 13 — full not-found unit coverage everywhere, not a subset. `AdventureRouteResolverTest` (Task 1) additionally covers all 6 resolver methods' not-found behavior in isolation. Task 16's Playwright pass still live-verifies the same behavior end-to-end in a real browser — belt and suspenders, not a substitute, per the P1.3 lesson that only real navigation proves this class of fix.
7. **Route-parameter keys always come from `RouteIds`** (`com.pdg.adventure.view.support.RouteIds`: `ADVENTURE_ID`, `LOCATION_ID`, `COMMAND_ID`, `DIRECTION_ID`, `MESSAGE_ID`, `ITEM_ID`) — never string literals.
8. **Not-found notification wording matches the existing precedent exactly**, from `AdventureEditorView.loadAdventure`: `Notification.show("<Kind> not found or access denied: " + id, 5000, Notification.Position.MIDDLE)` then `notification.addThemeVariants(NotificationVariant.LUMO_ERROR)`.
9. **`DirectionEditorView.setData(LocationData, AdventureData)` takes reversed argument order** vs. `DirectionsMenuView.setData(AdventureData, LocationData)`. The new `beforeEnter` code in both files uses named fields, not positional arguments, so this trap doesn't matter for the rewrite — but don't copy one file's parameter order into the other's population helper call by habit.
10. **Preserve all existing UI-population logic byte-for-byte** — grid setup, counters, context menus, lazy editor construction. Only change WHAT populates it (resolver instead of caller-handed object) and WHEN (`beforeEnter` instead of `setData`), by calling the view's existing `setData(...)`/`setAdventureData(...)` method directly from the new `beforeEnter` with the resolver's result. Do not duplicate, inline, or restructure that method's body, even when it's more than a trivial field-set (e.g. `LocationEditorView.setData` derives `locationData`, populates vocabulary selectors, and builds a view model — all of that stays exactly as it is; `beforeEnter` just becomes a second caller of the same method, alongside the not-yet-removed external one).
11. **Every per-view test needs `SecurityContextHolder` set up**, even views whose old `beforeEnter` never needed it — because the resolver's `ViewSupporter.getCurrentUser()` call is now transitively reached by every one of the 13 views. In `@BeforeEach`: construct a minimal `UserData` (no need for ID reflection tricks — `AdventureAccessService` is always mocked in these tests, so the user object's contents are never actually inspected) and call `SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()))`. **`testUser.setRoles(Set.of())` must be called before `.getAuthorities()`** — `UserData.roles` (a hand-written `@Entity`, no Lombok default) is `null` on a bare `new UserData()`, and `getAuthorities()` unconditionally streams it, so omitting this NPEs every single one of these tests (confirmed empirically in Task 1; requires `import java.util.Set;`). Add an `@AfterEach` that calls `SecurityContextHolder.clearContext()`.
12. **Model tier for every implementer dispatch in this plan: sonnet** (or the session's equivalent standard-tier model), not a cheap/fast tier. Every task touches Spring's access-control wiring (`AdventureAccessService`) — correctness here is security-relevant, not purely mechanical.
13. **Every constructor-signature change ripples to that view's existing test file(s).** Adding `AdventureAccessService` as a new constructor parameter means any pre-existing test that directly does `new SomeView(mockAdventureService)` fails to compile. After making the production change, run the full test module (not just the new `RoutingTest`) and fix any resulting compile error in an existing test by adding a mock `AdventureAccessService` argument to its `new SomeView(...)` calls — this is an expected, in-scope, mechanical consequence of the signature change, not a sign of a mistake. Include the fixed existing test file in the task's commit.

---

### Task 1: `AdventureRouteResolver`

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/view/support/AdventureRouteResolver.java`
- Test: `server/src/test/java/com/pdg/adventure/view/support/AdventureRouteResolverTest.java`

**Interfaces:**
- Consumes: `AdventureAccessService.findAdventureById(String, UserData)` → `Optional<AdventureData>` (`com.pdg.adventure.server.security.service.AdventureAccessService`, already exists). `ViewSupporter.getCurrentUser()` → `UserData` (`com.pdg.adventure.view.support.ViewSupporter`, already exists). `RouteIds` enum (already exists, see Global Constraints §7).
- Produces (used by every later task):
  ```java
  Optional<AdventureData> resolveAdventure(BeforeEnterEvent event, AdventureAccessService accessService)
  Optional<LocationData>  resolveLocation(AdventureData adventure, BeforeEnterEvent event)
  Optional<ItemData>      resolveItem(LocationData location, BeforeEnterEvent event)
  Optional<DirectionData> resolveDirection(LocationData location, BeforeEnterEvent event)
  Optional<MessageData>   resolveMessage(AdventureData adventure, BeforeEnterEvent event)
  Optional<CommandChainData> resolveCommandChain(ThingData thing, BeforeEnterEvent event)
  ```
  All package `com.pdg.adventure.view.support.AdventureRouteResolver`. `ThingData` (`com.pdg.adventure.model.ThingData`) is the common superclass of `LocationData` and `ItemData` — pass whichever one is in scope (e.g. `itemData != null ? itemData : locationData`, matching the existing `CommandsMenuView`/`CommandEditorView` branch).

**This task also settles an open empirical question for the rest of the plan (Global Constraints §6): whether `Notification.show(...)` throws or silently no-ops outside a live UI context.** This class's own not-found path calls it, and this task's test exercises that path inside `BrowserlessTest`, which provides a live UI — so the test below either passes (confirming `BrowserlessTest` gives `Notification.show(...)` a working context, as `AdventureAssignmentViewTest` already demonstrates elsewhere in this codebase) or fails in a way that surfaces the answer directly. That's not an additional step; it falls out of writing the test below normally.

- [ ] **Step 1: Write the failing tests**

Create `server/src/test/java/com/pdg/adventure/view/support/AdventureRouteResolverTest.java`:

```java
package com.pdg.adventure.view.support;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;

class AdventureRouteResolverTest extends BrowserlessTest {

    private AdventureAccessService accessService;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    // --- resolveAdventure ---

    @Test
    void resolveAdventure_validId_returnsAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"));

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventure(event, accessService);

        assertThat(result).contains(adventure);
    }

    @Test
    void resolveAdventure_unknownId_returnsEmptyAndShowsNotification() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"));

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventure(event, accessService);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void resolveAdventure_missingRouteParam_returnsEmptyWithoutCallingAccessService() {
        BeforeEnterEvent event = eventWithParams();

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventure(event, accessService);

        assertThat(result).isEmpty();
        verify(accessService, never()).findAdventureById(any(), any());
    }

    // --- resolveLocation ---

    @Test
    void resolveLocation_validId_returnsLocation() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        AdventureData adventure = new AdventureData();
        adventure.setLocationData(Map.of("loc-1", location));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        Optional<LocationData> result = AdventureRouteResolver.resolveLocation(adventure, event);

        assertThat(result).contains(location);
    }

    @Test
    void resolveLocation_unknownId_returnsEmptyAndShowsNotification() {
        AdventureData adventure = new AdventureData();
        adventure.setLocationData(Map.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        Optional<LocationData> result = AdventureRouteResolver.resolveLocation(adventure, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Location not found or access denied: missing");
    }

    // --- resolveItem ---

    @Test
    void resolveItem_validId_returnsItem() {
        ItemData item = new ItemData();
        item.setId("item-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setItemContainerData(container);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1"));

        Optional<ItemData> result = AdventureRouteResolver.resolveItem(location, event);

        assertThat(result).contains(item);
    }

    @Test
    void resolveItem_unknownId_returnsEmptyAndShowsNotification() {
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of());
        LocationData location = new LocationData();
        location.setItemContainerData(container);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ITEM_ID.getValue(), "missing"));

        Optional<ItemData> result = AdventureRouteResolver.resolveItem(location, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Item not found or access denied: missing");
    }

    // --- resolveDirection ---

    @Test
    void resolveDirection_validId_returnsDirection() {
        DirectionData direction = new DirectionData();
        direction.setId("dir-1");
        LocationData location = new LocationData();
        location.setDirectionsData(Set.of(direction));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.DIRECTION_ID.getValue(), "dir-1"));

        Optional<DirectionData> result = AdventureRouteResolver.resolveDirection(location, event);

        assertThat(result).contains(direction);
    }

    @Test
    void resolveDirection_unknownId_returnsEmptyAndShowsNotification() {
        LocationData location = new LocationData();
        location.setDirectionsData(Set.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.DIRECTION_ID.getValue(), "missing"));

        Optional<DirectionData> result = AdventureRouteResolver.resolveDirection(location, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Direction not found or access denied: missing");
    }

    // --- resolveMessage ---

    @Test
    void resolveMessage_validId_returnsMessage() {
        MessageData message = new MessageData();
        AdventureData adventure = new AdventureData();
        adventure.setMessages(Map.of("msg-1", message));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.MESSAGE_ID.getValue(), "msg-1"));

        Optional<MessageData> result = AdventureRouteResolver.resolveMessage(adventure, event);

        assertThat(result).contains(message);
    }

    @Test
    void resolveMessage_unknownId_returnsEmptyAndShowsNotification() {
        AdventureData adventure = new AdventureData();
        adventure.setMessages(Map.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.MESSAGE_ID.getValue(), "missing"));

        Optional<MessageData> result = AdventureRouteResolver.resolveMessage(adventure, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Message not found or access denied: missing");
    }

    // --- resolveCommandChain ---

    @Test
    void resolveCommandChain_validId_returnsChain() {
        CommandChainData chain = new CommandChainData();
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of("go|north|", chain));
        LocationData location = new LocationData();
        location.setCommandProviderData(provider);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.COMMAND_ID.getValue(), "go|north|"));

        Optional<CommandChainData> result = AdventureRouteResolver.resolveCommandChain(location, event);

        assertThat(result).contains(chain);
    }

    @Test
    void resolveCommandChain_unknownId_returnsEmptyAndShowsNotification() {
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of());
        LocationData location = new LocationData();
        location.setCommandProviderData(provider);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.COMMAND_ID.getValue(), "missing"));

        Optional<CommandChainData> result = AdventureRouteResolver.resolveCommandChain(location, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Command not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && mvn test -Dtest=AdventureRouteResolverTest`
Expected: compile failure (`AdventureRouteResolver` does not exist yet).

- [ ] **Step 3: Write the implementation**

Create `server/src/main/java/com/pdg/adventure/view/support/AdventureRouteResolver.java`:

```java
package com.pdg.adventure.view.support;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEnterEvent;

import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.ThingData;
import com.pdg.adventure.server.security.service.AdventureAccessService;

/**
 * Resolves domain objects from a navigation event's route parameters, access-checked
 * where applicable. Every method shows a "not found or access denied" notification
 * and returns {@code Optional.empty()} on failure; callers decide where to forward.
 */
public final class AdventureRouteResolver {

    private AdventureRouteResolver() {
    }

    public static Optional<AdventureData> resolveAdventure(BeforeEnterEvent event,
                                                             AdventureAccessService accessService) {
        Optional<String> adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        if (adventureId.isEmpty()) {
            return Optional.empty();
        }
        Optional<AdventureData> adventure = accessService.findAdventureById(adventureId.get(),
                ViewSupporter.getCurrentUser());
        if (adventure.isEmpty()) {
            showNotFound("Adventure", adventureId.get());
        }
        return adventure;
    }

    public static Optional<LocationData> resolveLocation(AdventureData adventure, BeforeEnterEvent event) {
        Optional<String> locationId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (locationId.isEmpty()) {
            return Optional.empty();
        }
        LocationData location = adventure.getLocationData().get(locationId.get());
        if (location == null) {
            showNotFound("Location", locationId.get());
            return Optional.empty();
        }
        return Optional.of(location);
    }

    public static Optional<ItemData> resolveItem(LocationData location, BeforeEnterEvent event) {
        Optional<String> itemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (itemId.isEmpty()) {
            return Optional.empty();
        }
        Optional<ItemData> item = location.getItemContainerData().getItems().stream()
                .filter(candidate -> candidate.getId().equals(itemId.get()))
                .findFirst();
        if (item.isEmpty()) {
            showNotFound("Item", itemId.get());
        }
        return item;
    }

    public static Optional<DirectionData> resolveDirection(LocationData location, BeforeEnterEvent event) {
        Optional<String> directionId = event.getRouteParameters().get(RouteIds.DIRECTION_ID.getValue());
        if (directionId.isEmpty()) {
            return Optional.empty();
        }
        Optional<DirectionData> direction = location.getDirectionsData().stream()
                .filter(candidate -> candidate.getId().equals(directionId.get()))
                .findFirst();
        if (direction.isEmpty()) {
            showNotFound("Direction", directionId.get());
        }
        return direction;
    }

    public static Optional<MessageData> resolveMessage(AdventureData adventure, BeforeEnterEvent event) {
        Optional<String> messageId = event.getRouteParameters().get(RouteIds.MESSAGE_ID.getValue());
        if (messageId.isEmpty()) {
            return Optional.empty();
        }
        MessageData message = adventure.getMessages().get(messageId.get());
        if (message == null) {
            showNotFound("Message", messageId.get());
            return Optional.empty();
        }
        return Optional.of(message);
    }

    public static Optional<CommandChainData> resolveCommandChain(ThingData thing, BeforeEnterEvent event) {
        Optional<String> commandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        if (commandId.isEmpty()) {
            return Optional.empty();
        }
        CommandChainData chain = thing.getCommandProviderData().getAvailableCommands().get(commandId.get());
        if (chain == null) {
            showNotFound("Command", commandId.get());
            return Optional.empty();
        }
        return Optional.of(chain);
    }

    private static void showNotFound(String kind, String id) {
        Notification notification = Notification.show(
                kind + " not found or access denied: " + id, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd server && mvn test -Dtest=AdventureRouteResolverTest`
Expected: `Tests run: 13, Failures: 0, Errors: 0` (all pass). If any `resolveXxx_unknownId_...` test throws instead of passing, `Notification.show(...)` does NOT tolerate a missing UI gracefully in this Vaadin version even under `BrowserlessTest` in the way expected — stop and re-read the `BrowserlessTest`/`AdventureAssignmentViewTest` precedent (`server/src/test/java/com/pdg/adventure/view/admin/AdventureAssignmentViewTest.java`) rather than working around it; this would mean something about the setup (not the resolver's logic) is wrong.

- [ ] **Step 5: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/view/support/AdventureRouteResolver.java src/test/java/com/pdg/adventure/view/support/AdventureRouteResolverTest.java
git commit -m "feat: add AdventureRouteResolver for URL-driven view population"
```

---

### Task 2: `LocationsMenuView`

This is the messiest of the 13 views — its `beforeEnter` exists but its entire body is commented out, and it's reachable via a parameter-free `@RouteAlias`. Read this task fully even if you're implementing a later one; it's the template the rest follow.

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/location/LocationsMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/location/LocationsMenuViewTest.java` (create — no test file exists for this view today)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure(BeforeEnterEvent, AdventureAccessService)` from Task 1.
- Produces: nothing later tasks depend on — each view task is independent.

**Do NOT in this task:** remove the `@RouteAlias(value = "author/adventures/locations", ...)` on the class, and do NOT touch `AdventureEditorView.java:59-61` or `LocationEditorView.java:202-205` (the two callers that navigate here with no route parameters). That's Task 15. This task only fixes what happens once a request WITH an `:adventureId` reaches this view.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/location/LocationsMenuViewTest.java`:

```java
package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class LocationsMenuViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private LocationsMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new LocationsMenuView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_populatesLocationsGrid() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=LocationsMenuViewTest`
Expected: compile failure — `LocationsMenuView(AdventureService, AdventureAccessService)` constructor doesn't exist yet (current constructor takes only `AdventureService`).

- [ ] **Step 3: Modify `LocationsMenuView.java`**

Add two imports (alongside the existing `import com.pdg.adventure.view.support.RouteIds;` / `ViewSupporter` lines):

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to the existing `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first two assignment lines from:

```java
    public LocationsMenuView(AdventureService anAdventureService) {

        setSizeFull();

        adventureService = anAdventureService;
```

to:

```java
    public LocationsMenuView(AdventureService anAdventureService, AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
```

Replace the entire commented-out `beforeEnter` method:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
//        Optional<String> adventureId = event.getRouteParameters().get("adventureId");
//        Objects.requireNonNull(adventureId);
//
//        if (adventureId.isPresent()) {
//            setUpLoading(adventureId.get());
//        } else {
//            setUpNewEdit();
//        }
//        fillGUI();
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        setAdventureData(resolvedAdventure.get());
    }
```

Delete the now-superseded dead method entirely (it has zero callers anywhere in the codebase — confirmed by `grep -rn "\.loadAdventure("` across `main` and `test`, whose only match is an unrelated console client):

```java
    public void loadAdventure(String anAdventureId) {
        Optional<AdventureData> loadedAdventure = adventureService.findAdventureById(anAdventureId);
        if (loadedAdventure.isPresent()) {
            adventureData = loadedAdventure.get();
            binder.setBean(adventureData);
        }
    }
```

Leave `setAdventureData(AdventureData)` and `fillGUI()` exactly as they are (Global Constraint §10) — `beforeEnter` now calls `setAdventureData(...)` directly, the same method the external caller still calls too until Task 15.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=LocationsMenuViewTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Run the full existing test suite for this view's package to check for regressions**

Run: `cd server && mvn test -Dtest=com.pdg.adventure.view.location.*`
Expected: all pass (this package's other classes, e.g. any location-adjacent tests, are unaffected by this task — `LocationEditorView` is fixed in Task 3, not here).

- [ ] **Step 6: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/view/location/LocationsMenuView.java src/test/java/com/pdg/adventure/view/location/LocationsMenuViewTest.java
git commit -m "fix: LocationsMenuView resolves adventure from URL in beforeEnter"
```

---

### Task 3: `LocationEditorView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/location/LocationEditorView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/location/LocationEditorViewRoutingTest.java` (create — do not read or modify the existing `LocationEditorViewBrowserlessTest.java`)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure(BeforeEnterEvent, AdventureAccessService)` from Task 1.

**Deliberate scope note — read before writing `beforeEnter`:** this view's existing `setData(AdventureData)` resolves the location leniently: `adventureData.getLocationData().getOrDefault(locationId, new LocationData())`. An unknown `:locationId` silently becomes a blank "new location" form rather than an error — this is how the `/new` route alias (no `:locationId` at all) already works today, and the same fallback happens to also catch a genuinely-bad locationId. That's a pre-existing quirk, not something this task fixes or should change (Global Constraint §10 — preserve behavior byte-for-byte). Only ADVENTURE resolution gets a hard not-found/forward in this task; location resolution stays exactly as lenient as it is today.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/location/LocationEditorViewRoutingTest.java`:

```java
package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class LocationEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private LocationEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new LocationEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesFormFromResolvedData() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        assertThat(find(TextField.class, view).withValue("loc-1").exists()).isTrue();
        assertThat(find(TextField.class, view).withValue("adv-1").exists()).isTrue();
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=LocationEditorViewRoutingTest`
Expected: compile failure — `LocationEditorView(AdventureService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `LocationEditorView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventuresMainLayout;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public LocationEditorView(AdventureService anAdventureService) {

        setSizeFull();

        adventureService = anAdventureService;
        binder = new Binder<>(LocationViewModel.class);
```

to:

```java
    public LocationEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(LocationViewModel.class);
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalLocationId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (optionalLocationId.isPresent()) {
            locationId = optionalLocationId.get();
            pageTitle = "Edit Location #" + locationId;
        } else {
            pageTitle = "New Location";
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        final Optional<String> optionalLocationId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (optionalLocationId.isPresent()) {
            locationId = optionalLocationId.get();
            pageTitle = "Edit Location #" + locationId;
        } else {
            pageTitle = "New Location";
        }
        setData(resolvedAdventure.get());
    }
```

Leave `setData(AdventureData)` exactly as it is (Global Constraint §10) — `beforeEnter` now calls it directly.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=LocationEditorViewRoutingTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Run this view's full existing test suite to check for regressions**

Run: `cd server && mvn test -Dtest=LocationEditorViewBrowserlessTest,LocationEditorViewRoutingTest`
Expected: all pass. If `LocationEditorViewBrowserlessTest` fails, read it first — it may construct `LocationEditorView` directly with the old single-argument constructor and need updating to pass a mock `AdventureAccessService` too; that's an in-scope, expected fix (the constructor signature changed), not a sign this task did something wrong.

- [ ] **Step 6: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/view/location/LocationEditorView.java src/test/java/com/pdg/adventure/view/location/LocationEditorViewRoutingTest.java
git status
```

If Step 5 required updating `LocationEditorViewBrowserlessTest.java`, add it to the commit too:

```bash
git add src/test/java/com/pdg/adventure/view/location/LocationEditorViewBrowserlessTest.java
git commit -m "fix: LocationEditorView resolves adventure from URL in beforeEnter"
```

---

### Task 4: `DirectionsMenuView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/direction/DirectionsMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/direction/DirectionsMenuViewTest.java` (create — no test file exists for this view today)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` and `resolveLocation` from Task 1. `event.forwardTo(Class<? extends Component>, RouteParameters)` — confirmed real overload (`javap com.vaadin.flow.router.BeforeEvent`), used here for the first time in this plan since forwarding to `LocationsMenuView` requires supplying `:adventureId` (its canonical route requires it, and Task 15 removes the parameter-free alias this view could otherwise fall back to).

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/direction/DirectionsMenuViewTest.java`:

```java
package com.pdg.adventure.view.direction;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

class DirectionsMenuViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private DirectionsMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new DirectionsMenuView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesDirectionsGrid() {
        DirectionData direction = new DirectionData();
        direction.setId("dir-1");
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setDirectionsData(Set.of(direction));
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Location not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=DirectionsMenuViewTest`
Expected: compile failure — `DirectionsMenuView(AdventureService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `DirectionsMenuView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.location.LocationEditorView;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public DirectionsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();
```

to:

```java
    public DirectionsMenuView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        setSizeFull();
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> locId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (locId.isPresent()) {
            final String locationId = locId.get();
            pageTitle = "Exits for location #" + locationId;
        } else {
            pageTitle = "Exits";
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        Optional<String> locId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (locId.isPresent()) {
            pageTitle = "Exits for location #" + locId.get();
        } else {
            pageTitle = "Exits";
        }
        setData(resolvedAdventure.get(), resolvedLocation.get());
    }
```

Leave `setData(AdventureData, LocationData)` exactly as it is (Global Constraint §10) — `beforeEnter` now calls it directly.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=DirectionsMenuViewTest`
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

Per Global Constraint §13, check whether this constructor change broke any pre-existing test in this package first:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.direction.*
```

If anything else in that package fails to compile because it constructs `DirectionsMenuView` directly, fix it (add a mock `AdventureAccessService` argument) and include it in the commit.

```bash
git add src/main/java/com/pdg/adventure/view/direction/DirectionsMenuView.java src/test/java/com/pdg/adventure/view/direction/DirectionsMenuViewTest.java
git commit -m "fix: DirectionsMenuView resolves adventure and location from URL in beforeEnter"
```

---

### Task 5: `DirectionEditorView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/direction/DirectionEditorView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/direction/DirectionEditorViewRoutingTest.java` (create — do not read or modify the 5 existing `DirectionEditorView*Test.java` files beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` and `resolveLocation` from Task 1.

**⚠️ Argument-order trap (Global Constraint §9):** this view's existing `setData` signature is `setData(LocationData aLocationData, AdventureData anAdventureData)` — **location first, adventure second** — the reverse of `DirectionsMenuView.setData(AdventureData, LocationData)` from Task 4. The call you write in `beforeEnter` must be `setData(resolvedLocation.get(), resolvedAdventure.get())`, in that exact order. Getting this backwards compiles fine (both are object references) and silently swaps semantics.

**Existing test seams — do not remove:** this class has `protected void setUpLoading(String)` and `protected DirectionViewModel getViewModel()` methods (with a javadoc noting they exist specifically so existing tests can seed/inspect state without going through `beforeEnter`). Five existing test files may use them. This task doesn't touch them; it only adds the adventure/location-resolution prefix ahead of the direction-id logic `beforeEnter` already has.

**Deliberate scope note, matching Task 3's:** direction resolution stays exactly as lenient as it is today — `setData` derives `directionData` via `locationData.getDirectionsData().stream().filter(...).findFirst().orElse(new DirectionData())`, silently defaulting to a blank direction on any miss (this is also how the `/new` alias works). Only ADVENTURE and LOCATION resolution get a hard not-found/forward in this task.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/direction/DirectionEditorViewRoutingTest.java`:

```java
package com.pdg.adventure.view.direction;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

class DirectionEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private DirectionEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new DirectionEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesDestinationGridFromResolvedAdventure() {
        LocationData otherLocation = new LocationData();
        otherLocation.setId("loc-2");
        LocationData location = new LocationData();
        location.setId("loc-1");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location, "loc-2", otherLocation));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        Grid<?> destinationGrid = find(Grid.class, view).single();
        assertThat(test(destinationGrid).size()).isEqualTo(2);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Location not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=DirectionEditorViewRoutingTest`
Expected: compile failure — `DirectionEditorView(AdventureService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `DirectionEditorView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventuresMainLayout;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public DirectionEditorView(AdventureService anAdventureService) {

        setSizeFull();

        adventureService = anAdventureService;
        binder = new Binder<>(DirectionViewModel.class);
```

to:

```java
    public DirectionEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(DirectionViewModel.class);
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalDirectionId = event.getRouteParameters().get(RouteIds.DIRECTION_ID.getValue());
        if (optionalDirectionId.isPresent()) {
            directionId = optionalDirectionId.get();
            pageTitle = "Edit Direction #" + directionId;
        } else {
            pageTitle = "New Direction";
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        final Optional<String> optionalDirectionId = event.getRouteParameters().get(RouteIds.DIRECTION_ID.getValue());
        if (optionalDirectionId.isPresent()) {
            directionId = optionalDirectionId.get();
            pageTitle = "Edit Direction #" + directionId;
        } else {
            pageTitle = "New Direction";
        }
        setData(resolvedLocation.get(), resolvedAdventure.get());
    }
```

Note the final call: `setData(resolvedLocation.get(), resolvedAdventure.get())` — location, then adventure. Leave `setData(LocationData, AdventureData)` exactly as it is (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=DirectionEditorViewRoutingTest`
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into existing tests and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.direction.*
```

The 5 existing `DirectionEditorView*Test.java` files almost certainly construct `new DirectionEditorView(someAdventureService)` directly and will fail to compile. Fix each by adding a mock `AdventureAccessService` constructor argument — do not change anything else about them; their use of `setUpLoading(String)`/`getViewModel()` stays as-is.

```bash
git add src/main/java/com/pdg/adventure/view/direction/DirectionEditorView.java src/test/java/com/pdg/adventure/view/direction/
git commit -m "fix: DirectionEditorView resolves adventure and location from URL in beforeEnter"
```

---

### Task 6: `ItemsMenuView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/item/ItemsMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/item/ItemsMenuViewRoutingTest.java` (create — do not read or modify the existing `ItemsMenuViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` and `resolveLocation` from Task 1.

**Note:** this view's `@Route` has no `:itemId` segment (only `:adventureId`/`:locationId`) — the existing `beforeEnter`'s `optionalItemId` read is effectively always empty and only feeds grid pre-selection after the fact. Preserve it exactly; it's harmless and unrelated to this fix.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/item/ItemsMenuViewRoutingTest.java`:

```java
package com.pdg.adventure.view.item;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

class ItemsMenuViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;
    private ItemsMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new ItemsMenuView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesItemsGrid() {
        ItemData item = new ItemData();
        item.setId("item-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Location not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=ItemsMenuViewRoutingTest`
Expected: compile failure — `ItemsMenuView(AdventureService, ItemService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `ItemsMenuView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.location.LocationEditorView;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public ItemsMenuView(AdventureService anAdventureService, ItemService anItemService) {

        setSizeFull();

        adventureService = anAdventureService;
```

to:

```java
    public ItemsMenuView(AdventureService anAdventureService, ItemService anItemService,
                         AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());

        if (optionalItemId.isPresent()) {
            selectedItemId = optionalItemId.get();
        } else {
            selectedItemId = null;
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (optionalItemId.isPresent()) {
            selectedItemId = optionalItemId.get();
        } else {
            selectedItemId = null;
        }
        setData(resolvedAdventure.get(), resolvedLocation.get());
    }
```

Leave `setData(AdventureData, LocationData)` exactly as it is (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=ItemsMenuViewRoutingTest`
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.item.ItemsMenuViewTest,com.pdg.adventure.view.item.ItemsMenuViewRoutingTest
```

`ItemsMenuViewTest.java` almost certainly constructs `new ItemsMenuView(someAdventureService, someItemService)` directly — fix its call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/item/ItemsMenuView.java src/test/java/com/pdg/adventure/view/item/ItemsMenuViewTest.java src/test/java/com/pdg/adventure/view/item/ItemsMenuViewRoutingTest.java
git commit -m "fix: ItemsMenuView resolves adventure and location from URL in beforeEnter"
```

---

### Task 7: `AllItemsMenuView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/item/AllItemsMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/item/AllItemsMenuViewRoutingTest.java` (create — do not read or modify the existing `AllItemsMenuViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` from Task 1. This view's `@Route` carries only `:adventureId` — no location resolution needed.

**Note:** this view's current `beforeEnter` is a single comment (`// The setData method will be called from navigation`) — the clearest example in the codebase of the bug this whole plan fixes.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/item/AllItemsMenuViewRoutingTest.java`:

```java
package com.pdg.adventure.view.item;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class AllItemsMenuViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;
    private AllItemsMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new AllItemsMenuView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_populatesAllItemsGrid() {
        ItemData item = new ItemData();
        item.setId("item-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=AllItemsMenuViewRoutingTest`
Expected: compile failure — `AllItemsMenuView(AdventureService, ItemService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `AllItemsMenuView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventureEditorView;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public AllItemsMenuView(AdventureService anAdventureService, ItemService anItemService) {
        setSizeFull();

        adventureService = anAdventureService;
```

to:

```java
    public AllItemsMenuView(AdventureService anAdventureService, ItemService anItemService,
                            AdventureAccessService anAccessService) {
        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // The setData method will be called from navigation
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        setData(resolvedAdventure.get());
    }
```

Leave `setData(AdventureData)` exactly as it is (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=AllItemsMenuViewRoutingTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.item.AllItemsMenuViewTest,com.pdg.adventure.view.item.AllItemsMenuViewRoutingTest
```

Fix `AllItemsMenuViewTest.java`'s direct `new AllItemsMenuView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/item/AllItemsMenuView.java src/test/java/com/pdg/adventure/view/item/AllItemsMenuViewTest.java src/test/java/com/pdg/adventure/view/item/AllItemsMenuViewRoutingTest.java
git commit -m "fix: AllItemsMenuView resolves adventure from URL in beforeEnter"
```

---

### Task 8: `ItemEditorView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/item/ItemEditorView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/item/ItemEditorViewRoutingTest.java` (create — do not read or modify the existing `ItemEditorViewTest.java`/`ItemEditorViewBrowserlessTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` and `resolveLocation` from Task 1.

**Deliberate scope note, matching Tasks 3 and 5:** item resolution stays lenient — `setData` derives `itemData` via a stream-filter `.orElseGet(ItemData::new)`, silently defaulting to a blank new item on any miss (this is also how the `/new` alias works). Only ADVENTURE and LOCATION resolution get a hard not-found/forward here.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/item/ItemEditorViewRoutingTest.java`:

```java
package com.pdg.adventure.view.item;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

class ItemEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;
    private ItemEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new ItemEditorView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesFormFromResolvedItem() {
        ItemData item = new ItemData();
        item.setId("item-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1")));

        assertThat(find(TextField.class, view).withValue("item-1").exists()).isTrue();
        assertThat(find(TextField.class, view).withValue("loc-1").exists()).isTrue();
        assertThat(find(TextField.class, view).withValue("adv-1").exists()).isTrue();
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Location not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=ItemEditorViewRoutingTest`
Expected: compile failure — `ItemEditorView(AdventureService, ItemService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `ItemEditorView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventuresMainLayout;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public ItemEditorView(AdventureService anAdventureService, ItemService anItemService) {

        setSizeFull();

        adventureService = anAdventureService;
        itemService = anItemService;
```

to:

```java
    public ItemEditorView(AdventureService anAdventureService, ItemService anItemService,
                          AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        itemService = anItemService;
        accessService = anAccessService;
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());

        if (optionalItemId.isPresent()) {
            itemId = optionalItemId.get();
            pageTitle = "Edit Item #" + itemId;
        } else {
            pageTitle = "New Item";
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (optionalItemId.isPresent()) {
            itemId = optionalItemId.get();
            pageTitle = "Edit Item #" + itemId;
        } else {
            pageTitle = "New Item";
        }
        setData(resolvedAdventure.get(), resolvedLocation.get());
    }
```

Leave `setData(AdventureData, LocationData)` exactly as it is (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=ItemEditorViewRoutingTest`
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into existing tests and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.item.ItemEditorViewTest,com.pdg.adventure.view.item.ItemEditorViewBrowserlessTest,com.pdg.adventure.view.item.ItemEditorViewRoutingTest
```

Fix both existing files' direct `new ItemEditorView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/item/ItemEditorView.java src/test/java/com/pdg/adventure/view/item/ItemEditorViewTest.java src/test/java/com/pdg/adventure/view/item/ItemEditorViewBrowserlessTest.java src/test/java/com/pdg/adventure/view/item/ItemEditorViewRoutingTest.java
git commit -m "fix: ItemEditorView resolves adventure and location from URL in beforeEnter"
```

---

### Task 9: `CommandsMenuView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/command/CommandsMenuViewRoutingTest.java` (create — do not read or modify the existing `CommandsMenuViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure`, `resolveLocation`, and `resolveItem` from Task 1.

**The branching case — read carefully.** This view has two route shapes: `@Route(".../locations/:locationId/commands")` (location-scoped) and `@RouteAlias(".../locations/:locationId/items/:itemId/commands")` (item-scoped). The existing `beforeEnter` already branches on `:itemId` presence, but only to build `pageTitle` — the actual `itemData`/`commandProviderData` resolution happens exclusively in the caller-supplied `setData` overloads today. The new `beforeEnter` must resolve the SAME branch for real: adventure → location → (if `:itemId` present) item, each with its own forward-on-failure target, then call whichever `setData` overload matches.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/command/CommandsMenuViewRoutingTest.java`:

```java
package com.pdg.adventure.view.command;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.item.ItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

class CommandsMenuViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;
    private CommandsMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new CommandsMenuView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    private static CommandProviderData providerWithOneCommand() {
        CommandData command = new CommandData();
        command.setCommandDescription(new CommandDescriptionData("go|north|"));
        CommandChainData chain = new CommandChainData();
        chain.setCommands(List.of(command));
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of("go|north|", chain));
        return provider;
    }

    @Test
    void beforeEnter_locationScoped_validIds_populatesGridFromLocationCommands() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setCommandProviderData(providerWithOneCommand());
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        assertThat(view.getPageTitle()).isEqualTo("Commands for location #loc-1");
        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_itemScoped_validIds_populatesGridFromItemCommands() {
        ItemData item = new ItemData();
        item.setId("item-1");
        item.setCommandProviderData(providerWithOneCommand());
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1")));

        assertThat(view.getPageTitle()).isEqualTo("Commands for item #item-1");
        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
    }

    @Test
    void beforeEnter_unknownItemId_forwardsToItemsMenuViewForThatLocation() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(new ItemContainerData("loc-1"));
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(ItemsMenuView.class, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Item not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=CommandsMenuViewRoutingTest`
Expected: compile failure — `CommandsMenuView(AdventureService, ItemService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `CommandsMenuView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.item.ItemEditorView;` and `import com.pdg.adventure.view.location.LocationEditorView;` lines:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.item.ItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public CommandsMenuView(AdventureService anAdventureService, ItemService anItemService) {
        adventureService = anAdventureService;
        itemService = anItemService;
```

to:

```java
    public CommandsMenuView(AdventureService anAdventureService, ItemService anItemService,
                            AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        itemService = anItemService;
        accessService = anAccessService;
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (optionalItemId.isPresent()) {
            pageTitle = "Commands for item #" + optionalItemId.get();
        } else {
            String locationId = event.getRouteParameters().get(LOCATION_ID.getValue()).orElse("666");
            pageTitle = "Commands for location #" + locationId;
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (optionalItemId.isPresent()) {
            pageTitle = "Commands for item #" + optionalItemId.get();
            Optional<ItemData> resolvedItem = AdventureRouteResolver.resolveItem(resolvedLocation.get(), event);
            if (resolvedItem.isEmpty()) {
                event.forwardTo(ItemsMenuView.class, new RouteParameters(
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId()),
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), resolvedLocation.get().getId())));
                return;
            }
            setData(resolvedAdventure.get(), resolvedLocation.get(), resolvedItem.get());
        } else {
            String locationId = event.getRouteParameters().get(LOCATION_ID.getValue()).orElse("666");
            pageTitle = "Commands for location #" + locationId;
            setData(resolvedAdventure.get(), resolvedLocation.get());
        }
    }
```

Leave both `setData` overloads and `populate(...)` exactly as they are (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=CommandsMenuViewRoutingTest`
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.command.CommandsMenuViewTest,com.pdg.adventure.view.command.CommandsMenuViewRoutingTest
```

Fix `CommandsMenuViewTest.java`'s direct `new CommandsMenuView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java src/test/java/com/pdg/adventure/view/command/CommandsMenuViewTest.java src/test/java/com/pdg/adventure/view/command/CommandsMenuViewRoutingTest.java
git commit -m "fix: CommandsMenuView resolves adventure/location/item from URL in beforeEnter"
```

---

### Task 10: `CommandEditorView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/command/CommandEditorView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/command/CommandEditorViewRoutingTest.java` (create — do not read or modify the existing `CommandEditorViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure`, `resolveLocation`, and `resolveItem` from Task 1.

**Same item-vs-location branch as Task 9's `CommandsMenuView`, plus one more level.** This view has 4 route annotations (location+edit, location+new, item+edit, item+new). Unlike `CommandsMenuView`, the existing `beforeEnter` here doesn't read `:itemId` at all today — only `:commandId`. Add the same adventure → location → (if `:itemId` present) item resolution chain as Task 9, THEN keep the existing `:commandId` read exactly as it is (command resolution stays lenient — see the note below — so it's just a field read, not a resolver call).

**Deliberate scope note, matching Tasks 3, 5, and 8:** command resolution stays lenient. `populate()`'s `populateCommandChain()` already does `commandProviderData.getAvailableCommands().get(commandId)` and gracefully falls back to an empty grid / new `CommandData()` when the key isn't found — this is also how the `/new` alias works. Don't call `AdventureRouteResolver.resolveCommandChain(...)` here; keep reading `:commandId` directly, same as today.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/command/CommandEditorViewRoutingTest.java`:

```java
package com.pdg.adventure.view.command;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.item.ItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

class CommandEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;
    private CommandEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new CommandEditorView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    private static CommandProviderData providerWithOneCommand() {
        CommandData command = new CommandData();
        command.setCommandDescription(new CommandDescriptionData("go|north|"));
        CommandChainData chain = new CommandChainData();
        chain.setCommands(List.of(command));
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of("go|north|", chain));
        return provider;
    }

    @Test
    void beforeEnter_locationScoped_validIds_populatesChainGridFromLocationCommands() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setCommandProviderData(providerWithOneCommand());
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.COMMAND_ID.getValue(), "go|north|")));

        assertThat(view.getPageTitle()).isEqualTo("Edit Command #go|north|");
        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_itemScoped_validIds_populatesChainGridFromItemCommands() {
        ItemData item = new ItemData();
        item.setId("item-1");
        item.setCommandProviderData(providerWithOneCommand());
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1"),
                new RouteParam(RouteIds.COMMAND_ID.getValue(), "go|north|")));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
    }

    @Test
    void beforeEnter_unknownItemId_forwardsToItemsMenuViewForThatLocation() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(new ItemContainerData("loc-1"));
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(ItemsMenuView.class, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Item not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=CommandEditorViewRoutingTest`
Expected: compile failure — `CommandEditorView(AdventureService, ItemService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `CommandEditorView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventuresMainLayout;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.item.ItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public CommandEditorView(AdventureService anAdventureService, ItemService anItemService) {
        adventureService = anAdventureService;
        itemService = anItemService;
```

to:

```java
    public CommandEditorView(AdventureService anAdventureService, ItemService anItemService,
                             AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        itemService = anItemService;
        accessService = anAccessService;
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        if (optionalCommandId.isPresent()) {
            commandId = optionalCommandId.get();
            pageTitle = "Edit Command #" + commandId;
        } else {
            pageTitle = "New Command";
        }

    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        Optional<ItemData> resolvedItem = Optional.empty();
        if (optionalItemId.isPresent()) {
            resolvedItem = AdventureRouteResolver.resolveItem(resolvedLocation.get(), event);
            if (resolvedItem.isEmpty()) {
                event.forwardTo(ItemsMenuView.class, new RouteParameters(
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId()),
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), resolvedLocation.get().getId())));
                return;
            }
        }
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        if (optionalCommandId.isPresent()) {
            commandId = optionalCommandId.get();
            pageTitle = "Edit Command #" + commandId;
        } else {
            pageTitle = "New Command";
        }
        if (resolvedItem.isPresent()) {
            setData(resolvedAdventure.get(), resolvedLocation.get(), resolvedItem.get());
        } else {
            setData(resolvedAdventure.get(), resolvedLocation.get());
        }
    }
```

Leave both `setData` overloads, `populate(...)`, and `populateCommandChain()` exactly as they are (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=CommandEditorViewRoutingTest`
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.command.CommandEditorViewTest,com.pdg.adventure.view.command.CommandEditorViewRoutingTest
```

Fix `CommandEditorViewTest.java`'s direct `new CommandEditorView(...)` call sites to also pass a mock `AdventureAccessService`. Its `protected void setUpLoading(String)` seam (if present, mirroring `DirectionEditorView`'s) stays as-is.

```bash
git add src/main/java/com/pdg/adventure/view/command/CommandEditorView.java src/test/java/com/pdg/adventure/view/command/CommandEditorViewTest.java src/test/java/com/pdg/adventure/view/command/CommandEditorViewRoutingTest.java
git commit -m "fix: CommandEditorView resolves adventure/location/item from URL in beforeEnter"
```

---

### Task 11: `VocabularyMenuView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/vocabulary/VocabularyMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/vocabulary/VocabularyMenuViewRoutingTest.java` (create — do not read or modify the existing `VocabularyMenuViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` from Task 1.

**This view has no `beforeEnter` at all today** — it only implements `SaveListener, GuiListener`. This task adds `BeforeEnterObserver` to its `implements` clause and adds the method from scratch. Its import block already has `import com.vaadin.flow.router.*;` (wildcard), so `BeforeEnterObserver`/`BeforeEnterEvent` need no new import.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/vocabulary/VocabularyMenuViewRoutingTest.java`:

```java
package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class VocabularyMenuViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private VocabularyMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new VocabularyMenuView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_populatesVocabularyGrid() {
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.addWord(new Word("sword", Word.Type.NOUN));
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setVocabularyData(vocabulary);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

`VocabularyData.addWord(Word)` and `Word(String aText, Word.Type aType)` — confirmed via direct read of `VocabularyData.java`/`Word.java` (note the constructor takes text first, type second).

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=VocabularyMenuViewRoutingTest`
Expected: compile failure — `VocabularyMenuView(AdventureService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `VocabularyMenuView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventureEditorView;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Change the class declaration from:

```java
public class VocabularyMenuView extends VerticalLayout implements SaveListener, GuiListener {
```

to:

```java
public class VocabularyMenuView extends VerticalLayout implements SaveListener, GuiListener, BeforeEnterObserver {
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public VocabularyMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();
        createGUI();
    }
```

to:

```java
    public VocabularyMenuView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        setSizeFull();
        createGUI();
    }
```

Add the new `beforeEnter` method (there is no existing one to replace — add this as a new method, e.g. right after the constructor):

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        setAdventureData(resolvedAdventure.get());
    }
```

This file's imports don't currently include `java.util.Optional` — check first; if it's missing, add `import java.util.Optional;` alongside the existing `import java.util.ArrayList;` / `import java.util.List;` lines.

Leave `setAdventureData(AdventureData)` and `updateGui()` exactly as they are (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=VocabularyMenuViewRoutingTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.vocabulary.VocabularyMenuViewTest,com.pdg.adventure.view.vocabulary.VocabularyMenuViewRoutingTest
```

Fix `VocabularyMenuViewTest.java`'s direct `new VocabularyMenuView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/vocabulary/VocabularyMenuView.java src/test/java/com/pdg/adventure/view/vocabulary/VocabularyMenuViewTest.java src/test/java/com/pdg/adventure/view/vocabulary/VocabularyMenuViewRoutingTest.java
git commit -m "fix: VocabularyMenuView resolves adventure from URL in beforeEnter"
```

---

### Task 12: `SpecialWordsView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/vocabulary/SpecialWordsView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/vocabulary/SpecialWordsViewRoutingTest.java` (create — do not read or modify the existing `SpecialWordsViewTest.java` beyond the constructor-argument fix in Global Constraint §13, even though it already extends `BrowserlessTest`)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` from Task 1.

**No `beforeEnter` exists today, and unlike `VocabularyMenuView` this file does NOT use a wildcard router import** — it imports `PageTitle`, `Route`, `RouteParam`, `RouteParameters` individually. You need explicit new imports for `BeforeEnterObserver`/`BeforeEnterEvent`. This file also has no existing `java.util.Optional` import — check and add it.

**Test design note:** `updateGui()` populates four `VocabularyPickerField` instances (`takeSelector`, `dropSelector`, `loadSelector`, `examineSelector`) — a custom component whose internal `BrowserlessTest` query/tester surface isn't established elsewhere in this plan. Rather than guess at an unconfirmed API, the happy-path test below verifies the resolver was called with the exact route-supplied id (proving extraction, per Global Constraint §5) and that no forward occurred — sufficient proof `beforeEnter` succeeded without needing to inspect the selector internals.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/vocabulary/SpecialWordsViewRoutingTest.java`:

```java
package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class SpecialWordsViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private SpecialWordsView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new SpecialWordsView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_resolvesAndPopulatesWithoutForwarding() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithAdventureId("adv-1");

        view.beforeEnter(event);

        verify(accessService).findAdventureById(eq("adv-1"), any(UserData.class));
        verify(event, never()).forwardTo(any(Class.class));
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=SpecialWordsViewRoutingTest`
Expected: compile failure — `SpecialWordsView(AdventureService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `SpecialWordsView.java`**

Add imports, alongside the existing individual `com.vaadin.flow.router.*` imports (`PageTitle`, `Route`, `RouteParam`, `RouteParameters`):

```java
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
```

Add, alongside the existing `import java.util.List;` / `import java.util.function.Consumer;` lines:

```java
import java.util.Optional;
```

Add, alongside the other `com.pdg.adventure.*` imports:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Change the class declaration from:

```java
public class SpecialWordsView extends VerticalLayout implements SaveListener, GuiListener {
```

to:

```java
public class SpecialWordsView extends VerticalLayout implements SaveListener, GuiListener, BeforeEnterObserver {
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first assignment line from:

```java
    public SpecialWordsView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();
        createGUI();
    }
```

to:

```java
    public SpecialWordsView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        setSizeFull();
        createGUI();
    }
```

Add the new `beforeEnter` method (there is no existing one to replace — add this as a new method, e.g. right after the constructor):

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        setAdventureData(resolvedAdventure.get());
    }
```

Leave `setAdventureData(AdventureData)` and `updateGui()` exactly as they are (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=SpecialWordsViewRoutingTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.vocabulary.SpecialWordsViewTest,com.pdg.adventure.view.vocabulary.SpecialWordsViewRoutingTest
```

Fix `SpecialWordsViewTest.java`'s direct `new SpecialWordsView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/vocabulary/SpecialWordsView.java src/test/java/com/pdg/adventure/view/vocabulary/SpecialWordsViewTest.java src/test/java/com/pdg/adventure/view/vocabulary/SpecialWordsViewRoutingTest.java
git commit -m "fix: SpecialWordsView resolves adventure from URL in beforeEnter"
```

---

### Task 13: `MessagesMenuView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/message/MessagesMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/message/MessagesMenuViewRoutingTest.java` (create — do not read or modify the existing `MessagesMenuViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` from Task 1.

**Constructor parameter order is unusual — preserve it.** This view's constructor is `MessagesMenuView(MessageService aMessageService, AdventureService anAdventureService)` — `MessageService` first. Append `AdventureAccessService` as a third parameter; don't reorder the existing two.

**Do not touch the `messageCount` listener registration** in the constructor (`grid.getDataProvider().addDataProviderListener(...)`) — it's registered once at construction against whatever data provider the grid starts with, separate from `setData`/`refreshGrid`'s population logic. This task doesn't change that wiring at all, only what calls into `setData`.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/message/MessagesMenuViewRoutingTest.java`:

```java
package com.pdg.adventure.view.message;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class MessagesMenuViewRoutingTest extends BrowserlessTest {

    private MessageService messageService;
    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private MessagesMenuView view;

    @BeforeEach
    void setUp() {
        messageService = mock(MessageService.class);
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new MessagesMenuView(messageService, adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_populatesMessagesGrid() {
        MessageData message = new MessageData();
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setMessages(Map.of("msg-1", message));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=MessagesMenuViewRoutingTest`
Expected: compile failure — `MessagesMenuView(MessageService, AdventureService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `MessagesMenuView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventureEditorView;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first two assignment lines from:

```java
    public MessagesMenuView(MessageService aMessageService, AdventureService anAdventureService) {
        messageService = aMessageService;
        adventureService = anAdventureService;
        setSizeFull();
```

to:

```java
    public MessagesMenuView(MessageService aMessageService, AdventureService anAdventureService,
                            AdventureAccessService anAccessService) {
        messageService = aMessageService;
        adventureService = anAdventureService;
        accessService = anAccessService;
        setSizeFull();
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        if (adventureId.isPresent()) {
            pageTitle = "Messages for Adventure #" + adventureId.get();
        } else {
            pageTitle = "Messages";
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<String> adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        if (adventureId.isPresent()) {
            pageTitle = "Messages for Adventure #" + adventureId.get();
        } else {
            pageTitle = "Messages";
        }
        setData(resolvedAdventure.get());
    }
```

Leave `setData(AdventureData)` and `refreshGrid()` exactly as they are (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=MessagesMenuViewRoutingTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.message.MessagesMenuViewTest,com.pdg.adventure.view.message.MessagesMenuViewRoutingTest
```

Fix `MessagesMenuViewTest.java`'s direct `new MessagesMenuView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/message/MessagesMenuView.java src/test/java/com/pdg/adventure/view/message/MessagesMenuViewTest.java src/test/java/com/pdg/adventure/view/message/MessagesMenuViewRoutingTest.java
git commit -m "fix: MessagesMenuView resolves adventure from URL in beforeEnter"
```

---

### Task 14: `MessageEditorView`

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/message/MessageEditorView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/message/MessageEditorViewRoutingTest.java` (create — do not read or modify the existing `MessageEditorViewTest.java` beyond the constructor-argument fix in Global Constraint §13)

**Interfaces:**
- Consumes: `AdventureRouteResolver.resolveAdventure` from Task 1.

**Constructor parameter order note:** this view's constructor is `MessageEditorView(AdventureService anAdventureService, MessageService aMessageService)` — `AdventureService` first, the reverse of Task 13's `MessagesMenuView(MessageService, AdventureService)`. Append `AdventureAccessService` as a third parameter in each file matching its own existing order; don't cross-apply one file's order to the other.

**Deliberate scope note, matching Tasks 3, 5, 8, and 10:** message resolution stays lenient — `setData` derives the message via `adventureData.getMessages().getOrDefault(messageId, new MessageData())`, silently defaulting to a blank new message on any miss (this is also how the `/new` alias works). Only ADVENTURE resolution gets a hard not-found/forward here — the existing `:messageId` read (which already correctly branches for the create-new case) is otherwise untouched.

- [ ] **Step 1: Write the failing test**

Create `server/src/test/java/com/pdg/adventure/view/message/MessageEditorViewRoutingTest.java`:

```java
package com.pdg.adventure.view.message;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class MessageEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private MessageService messageService;
    private AdventureAccessService accessService;
    private MessageEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        messageService = mock(MessageService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new MessageEditorView(adventureService, messageService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesMessageTextFromResolvedAdventure() {
        MessageData message = new MessageData();
        message.setMessageText("Welcome!");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setMessages(Map.of("msg-1", message));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.MESSAGE_ID.getValue(), "msg-1")));

        TextArea messageText = find(TextArea.class, view).single();
        assertThat(test(messageText).getValue()).isEqualTo("Welcome!");
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.MESSAGE_ID.getValue(), "msg-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd server && mvn test -Dtest=MessageEditorViewRoutingTest`
Expected: compile failure — `MessageEditorView(AdventureService, MessageService, AdventureAccessService)` constructor doesn't exist yet.

- [ ] **Step 3: Modify `MessageEditorView.java`**

Add imports, alongside the existing `import com.pdg.adventure.view.adventure.AdventuresMainLayout;` line:

```java
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
```

Add a new field, next to `private final transient AdventureService adventureService;`:

```java
    private final transient AdventureAccessService accessService;
```

Change the constructor signature and its first two assignment lines from:

```java
    public MessageEditorView(AdventureService anAdventureService, MessageService aMessageService) {
        setSizeFull();

        adventureService = anAdventureService;
        messageService = aMessageService;
```

to:

```java
    public MessageEditorView(AdventureService anAdventureService, MessageService aMessageService,
                             AdventureAccessService anAccessService) {
        setSizeFull();

        adventureService = anAdventureService;
        messageService = aMessageService;
        accessService = anAccessService;
```

Replace `beforeEnter`:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalMessageId = event.getRouteParameters().get(RouteIds.MESSAGE_ID.getValue());
        if (optionalMessageId.isPresent()) {
            messageId = optionalMessageId.get();
            pageTitle = "Edit Message: " + messageId;
        } else {
            messageId = null;
            pageTitle = "New Message";
        }
    }
```

with:

```java
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        final Optional<String> optionalMessageId = event.getRouteParameters().get(RouteIds.MESSAGE_ID.getValue());
        if (optionalMessageId.isPresent()) {
            messageId = optionalMessageId.get();
            pageTitle = "Edit Message: " + messageId;
        } else {
            messageId = null;
            pageTitle = "New Message";
        }
        setData(resolvedAdventure.get());
    }
```

Leave `setData(AdventureData)` exactly as it is (Global Constraint §10).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd server && mvn test -Dtest=MessageEditorViewRoutingTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Fix the ripple into the existing test and commit**

Per Global Constraint §13:

```bash
cd server
mvn test -Dtest=com.pdg.adventure.view.message.MessageEditorViewTest,com.pdg.adventure.view.message.MessageEditorViewRoutingTest
```

Fix `MessageEditorViewTest.java`'s direct `new MessageEditorView(...)` call sites to also pass a mock `AdventureAccessService`.

```bash
git add src/main/java/com/pdg/adventure/view/message/MessageEditorView.java src/test/java/com/pdg/adventure/view/message/MessageEditorViewTest.java src/test/java/com/pdg/adventure/view/message/MessageEditorViewRoutingTest.java
git commit -m "fix: MessageEditorView resolves adventure from URL in beforeEnter"
```

---

### Task 15: Final cleanup sweep

Tasks 2–14 made every view self-sufficient via `beforeEnter`, but left every caller's `.ifPresent(editor -> editor.setXxx(...))` handoff in place (Global Constraint §3). This task removes them, downgrades each population method to `private` now that only its own `beforeEnter` calls it, and removes `LocationsMenuView`'s parameter-free `@RouteAlias`.

**Files (modify all):**
- `server/src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java`
- `server/src/main/java/com/pdg/adventure/view/location/LocationsMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/location/LocationEditorView.java`
- `server/src/main/java/com/pdg/adventure/view/direction/DirectionsMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/direction/DirectionEditorView.java`
- `server/src/main/java/com/pdg/adventure/view/item/ItemsMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/item/AllItemsMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/item/ItemEditorView.java`
- `server/src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/command/CommandEditorView.java`
- `server/src/main/java/com/pdg/adventure/view/vocabulary/VocabularyMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/vocabulary/SpecialWordsView.java`
- `server/src/main/java/com/pdg/adventure/view/message/MessagesMenuView.java`
- `server/src/main/java/com/pdg/adventure/view/message/MessageEditorView.java`

- [ ] **Step 1: The mechanical rule**

For every navigation call site that does `UI.getCurrent().navigate(SomeView.class, ...).ifPresent(editor -> editor.setXxx(...))`, delete the `.ifPresent(...)` clause entirely and keep the bare `navigate(...)` call. The target view's own `beforeEnter` (added in Tasks 2–14) now does that population itself, from the same route parameters the `navigate(...)` call already supplies. Three worked examples covering the shapes you'll see:

**Shape A — single-line lambda** (most call sites). `AdventureEditorView.java`, the "Manage Vocabulary" button:

```java
// before
UI.getCurrent().navigate(VocabularyMenuView.class,
                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                            adventureData.getId())))
  .ifPresent(editor -> editor.setAdventureData(adventureData));
// after
UI.getCurrent().navigate(VocabularyMenuView.class,
                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                            adventureData.getId())));
```

**Shape B — multi-statement lambda block.** `AllItemsMenuView.java`, `navigateToCreateItem(String)`:

```java
// before
private void navigateToCreateItem(String locationId) {
    UI.getCurrent().navigate(ItemEditorView.class,
                             new RouteParameters(new RouteParam(RouteIds.LOCATION_ID.getValue(), locationId),
                                                 new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                adventureData.getId()))).ifPresent(e -> {
        LocationData location = adventureData.getLocationData().get(locationId);
        e.setData(adventureData, location);
    });
}
// after
private void navigateToCreateItem(String locationId) {
    UI.getCurrent().navigate(ItemEditorView.class,
                             new RouteParameters(new RouteParam(RouteIds.LOCATION_ID.getValue(), locationId),
                                                 new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                adventureData.getId())));
}
```

**Shape C — no route parameters supplied today, and no `.ifPresent(...)` either (the ID-less bug).** `AdventureEditorView.java`, the "Manage Locations" button, and `LocationEditorView.java`'s `navigateBack()` — these are the only two call sites in the whole codebase that navigate to `LocationsMenuView` without supplying `:adventureId`. Both need `RouteParameters` ADDED (matching every other "Manage X" button's pattern), not just handoff-removed:

```java
// AdventureEditorView.java — before
Button editLocationsButton = new Button("Manage Locations");
editLocationsButton.addClickListener(_ -> {
    if (binder.writeBeanIfValid(adventureData)) {
        UI.getCurrent().navigate(LocationsMenuView.class)
          .ifPresent(editor -> editor.setAdventureData(adventureData));
    }
});
// AdventureEditorView.java — after
Button editLocationsButton = new Button("Manage Locations");
editLocationsButton.addClickListener(_ -> {
    if (binder.writeBeanIfValid(adventureData)) {
        UI.getCurrent().navigate(LocationsMenuView.class,
                                 new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())));
    }
});
```

```java
// LocationEditorView.java — before
private void navigateBack() {
    UI.getCurrent().navigate(LocationsMenuView.class)
      .ifPresent(editor -> editor.setAdventureData(adventureData));
    }
// LocationEditorView.java — after
private void navigateBack() {
    UI.getCurrent().navigate(LocationsMenuView.class,
                             new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                adventureData.getId())));
}
```

(`RouteParameters`/`RouteParam` are already imported in both files via the existing `import com.vaadin.flow.router.*;` wildcard — confirmed in Tasks 2 and 3's source excerpts. No new import needed for this specific change.)

**Before deleting any `.ifPresent(...)` lambda, read its full body first.** Every one of the sites below is expected to do nothing but hand data to the target view (Shape A/B), per the Tasks 1–14 research — but confirm it for each site as you reach it, don't assume. If a lambda does anything beyond calling `setXxx(...)`/`setData(...)` on the target (a log statement, a field update on the *caller*, a counter increment, anything with a side effect outside the target view), stop and flag it rather than deleting it silently — that side effect has no other home once the handoff is gone.

- [ ] **Step 2: Apply Shape A/B to every other call site**

This checklist was compiled from the Task 1–14 research and is believed complete, but is not the sole correctness mechanism — Step 6's build-until-clean loop is. Work through each:

| File | Method(s) with a call site to fix |
|---|---|
| `AdventureEditorView.java` | "Manage Vocabulary" button, "Manage Messages" button, "Manage Items" button (3 sites; "Manage Locations" was Shape C above) |
| `LocationsMenuView.java` | `edit` button, `create` button, `navigateToLocationEditor(String)` (3 sites; `backButton` already has no handoff) |
| `LocationEditorView.java` | `manageCommands` button, `manageItems` button, `manageExits` button (3 sites; `navigateBack()` was Shape C above) |
| `DirectionsMenuView.java` | `backButton`, `create` button, the grid double-click listener inside `getGrid()`, `navigateToDirectionEditor(String)` (4 sites) |
| `DirectionEditorView.java` | `navigateBack()` (1 site) |
| `ItemsMenuView.java` | `edit` button, `create` button, `backButton` (3 sites) |
| `AllItemsMenuView.java` | `navigateToCreateItem(String)` (Shape B above), `navigateToItemEditor(String, String)` (Shape B, same pattern) (2 sites; `backButton` already has no handoff) |
| `ItemEditorView.java` | `commandsButton`, `navigateBack()` (2 sites) |
| `CommandsMenuView.java` | `createButton` (both the `itemData != null` and `else` branches), `backButton` (both branches), `navigateToCommandEditor(String)` (both branches) (6 sites total) |
| `CommandEditorView.java` | `navigateBack()` (both the `itemData != null` and `else` branches) (2 sites) |
| `VocabularyMenuView.java` | `editSpecialWords` button (1 site; `back` button already has no handoff) |
| `SpecialWordsView.java` | `back` button (1 site) |
| `MessagesMenuView.java` | `createButton`, the grid double-click listener inside `createGrid()`, the context menu's "Edit" item inside `createContextMenu(...)`, `duplicateMessage(...)` (4 sites; `backButton` already has no handoff) |
| `MessageEditorView.java` | `navigateBack()` (1 site) |

- [ ] **Step 3: Verify every surviving call site supplies what the target's new `beforeEnter` reads**

This is the one check nothing else in this plan performs automatically. The build-until-clean loop (Step 6) catches a call site that references a method which no longer compiles — it does NOT catch a `navigate(Target.class, new RouteParameters(...))` call that compiles fine but omits or mis-supplies a parameter the target's `beforeEnter` reads. Vaadin's router itself only guards the segments a route pattern marks mandatory; it says nothing about a parameter a resolver call additionally expects.

For each site touched in Steps 1–2, open the target view's `beforeEnter` (written in Tasks 2–14) and list every `RouteIds` key it reads via `event.getRouteParameters().get(...)` or passes into an `AdventureRouteResolver` call. Then confirm the call site's `RouteParameters`/`RouteParam` literal supplies each one. Pay particular attention to:

- **`CommandsMenuView`/`CommandEditorView`'s optional `ITEM_ID`.** Before this plan, "is this an item-scoped or location-scoped command view" was carried by which overload of `setData(...)` the caller's `.ifPresent(...)` lambda invoked — not necessarily by the URL. Confirm every call site that intends item-scoped navigation actually includes a `RouteParam(RouteIds.ITEM_ID.getValue(), ...)` in its `RouteParameters`, not just `ADVENTURE_ID`/`LOCATION_ID`. A call site that drops `ITEM_ID` will still navigate successfully (the route pattern doesn't require it) and `beforeEnter` will silently resolve the location-scoped branch instead of the item-scoped one — no compile error, no thrown exception, just wrong data rendered.
- Any other call site where the pre-plan `.ifPresent(...)` lambda used a value that ISN'T already one of `ADVENTURE_ID`/`LOCATION_ID`/`ITEM_ID`/`DIRECTION_ID`/`MESSAGE_ID`/`COMMAND_ID` in that call site's existing `RouteParameters` — that value was flowing through the object handoff only, and now needs a route parameter to reach the target instead.

If you find a gap, add the missing `RouteParam(...)` to that call site (same pattern as Step 1's Shape C fix) rather than leaving it for Task 16 to discover — Task 16 is a spot-check across 13 URLs and two forward cases, not an exhaustive walk of every button, so it cannot be relied on to catch every instance of this class of bug.

- [ ] **Step 4: Downgrade each population method to `private`**

Once no external caller references it, change the modifier on each of these from `public` to `private` (method bodies unchanged):

- `LocationsMenuView.setAdventureData(AdventureData)`
- `LocationEditorView.setData(AdventureData)`
- `DirectionsMenuView.setData(AdventureData, LocationData)`
- `DirectionEditorView.setData(LocationData, AdventureData)`
- `ItemsMenuView.setData(AdventureData, LocationData)`
- `AllItemsMenuView.setData(AdventureData)`
- `ItemEditorView.setData(AdventureData, LocationData)`
- `CommandsMenuView.setData(AdventureData, LocationData, ItemData)` and `setData(AdventureData, LocationData)` (both overloads)
- `CommandEditorView.setData(AdventureData, LocationData, ItemData)` and `setData(AdventureData, LocationData)` (both overloads)
- `VocabularyMenuView.setAdventureData(AdventureData)`
- `SpecialWordsView.setAdventureData(AdventureData)`
- `MessagesMenuView.setData(AdventureData)`
- `MessageEditorView.setData(AdventureData)`

If the compiler now complains that a `RoutingTest`/`BrowserlessTest`/existing test file calls one of these directly (bypassing `beforeEnter`), that test is violating Global Constraint §4 and was written incorrectly in an earlier task — fix the TEST to drive the view through `beforeEnter` with a mocked `BeforeEnterEvent` instead, do not revert the method to `public` to work around it.

- [ ] **Step 5: Remove `LocationsMenuView`'s parameter-free `@RouteAlias`**

Change:

```java
@Route(value = "author/adventures/:adventureId/locations", layout = LocationsMainLayout.class)
@RouteAlias(value = "author/adventures/locations", layout = LocationsMainLayout.class)
@PageTitle("Locations")
@RolesAllowed("ROLE_AUTHOR")
public class LocationsMenuView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {
```

to:

```java
@Route(value = "author/adventures/:adventureId/locations", layout = LocationsMainLayout.class)
@PageTitle("Locations")
@RolesAllowed("ROLE_AUTHOR")
public class LocationsMenuView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {
```

Every call site that used to reach this alias (the two Shape C sites in Step 1) now supplies `:adventureId` explicitly, so the alias has no remaining legitimate use — and per this plan's whole premise, an ID-less URL to this view should no longer resolve to anything.

- [ ] **Step 6: Build until clean**

```bash
cd server
mvn test-compile
```

If this fails, the error names the file and line of a missed call site or a test that bypasses `beforeEnter` — fix it (Shape A/B removal, or fix the offending test per Step 4's note) and re-run. Repeat until `test-compile` succeeds. Then:

```bash
mvn test
```

Expected: the full suite passes — this exercises every `RoutingTest`/`BrowserlessTest` from Tasks 1–14 plus every pre-existing test across the whole module. Note what this does and doesn't prove: it confirms every `beforeEnter` still resolves correctly given the route parameters each `RoutingTest` hand-builds, and that nothing references a now-private method. It does NOT independently confirm Step 3's cross-check (a passing suite says nothing about whether a real call site's `RouteParameters` are complete) — that's why Step 3 is a distinct, manual step rather than folded into "the tests will catch it."

- [ ] **Step 7: Commit**

```bash
git add -A
git status
```

Review the file list before committing — this step touches every file in the plan's scope; confirm nothing unexpected is staged.

```bash
git commit -m "fix: remove now-dead setData handoff and ID-less LocationsMenuView alias"
```

---

### Task 16: Live verification pass

Per Global Constraint §6 and the P1.3 precedent this whole approach is modeled on: a mocked `BeforeEnterEvent` proves `beforeEnter`'s logic is correct in isolation, but cannot prove a real browser renders correctly on a cold load. This task is pure verification against the running app via Playwright — it does not change production code. If it finds a genuine regression, stop and report the specific URL/symptom rather than attempting a fix inline; that becomes a follow-up fix-and-reverify cycle, same as any other review finding.

**Prerequisite:** the app running at `http://localhost:8080`, log in as `admin`/`admin123`.

**The key technique, and why it matters:** navigate to each URL directly (Playwright `browser_navigate`), never by clicking through from a page already open in the same browser tab. Clicking a button and typing/pasting a URL are NOT equivalent for this bug: a click's `navigate()` call is followed by the calling view's `.ifPresent(...)` handoff running in the same JS-side session (before this plan, that's what populated the target view) — but a direct URL navigation has no prior calling view to run that handoff from. `browser_navigate` to a URL you already have in hand is the accurate stand-in for "user opens a bookmark" or "user refreshes the page," which is exactly the scenario this whole plan fixes.

- [ ] **Step 1: Discover real IDs by walking the app normally once**

Log in, open an existing adventure for editing, and click through to each of the 13 fixed views via their normal in-app buttons/links/double-clicks (Manage Locations → a location → Manage Exits/Manage Items/Manage Commands → etc.; Manage Vocabulary → Edit Special Words; Manage Messages → a message; Manage Items at the adventure level). Record the exact URL the browser address bar shows at each of these 13 stops. This single walkthrough also serves as the design spec's required regression pass for in-app navigation — if any button click now renders an empty view or throws, Task 15 missed a call site; stop and report it rather than continuing.

- [ ] **Step 2: Re-visit each of the 13 recorded URLs via direct navigation**

For each URL recorded in Step 1, call `browser_navigate` directly to it (do not click there from within the app) and take a snapshot. Verify real, non-empty content renders — specifically:

| View | What must be visible |
|---|---|
| `LocationsMenuView` | The locations grid has rows (assuming the adventure has ≥1 location) and the "Locations: N" counter shows a real count |
| `LocationEditorView` | The location's noun/adjective/description fields are populated, not blank |
| `DirectionsMenuView` | The exits grid renders (rows if any exits exist, or the "Create some exits." empty state — either is fine, it must not be a download prompt or crash) |
| `DirectionEditorView` | The direction's fields are populated and the destination-location grid has rows |
| `ItemsMenuView` | The items grid renders for that location |
| `AllItemsMenuView` | The "Total Items: N" counter shows a real count and the grid renders |
| `ItemEditorView` | The item's noun/adjective/description fields are populated |
| `CommandsMenuView` | The commands grid renders for that location (or item, if you captured an item-scoped URL) |
| `CommandEditorView` | The command chain grid and precondition/action editor render for that command |
| `VocabularyMenuView` | The vocabulary grid has rows |
| `SpecialWordsView` | The take/drop/load/examine word selectors are populated |
| `MessagesMenuView` | The messages grid has rows |
| `MessageEditorView` | The message text field is populated, not blank |

- [ ] **Step 3: Spot-check cascading forwards**

Pick a URL from Step 1 that includes a `:locationId` (e.g. an Items or Exits or Commands URL) and hand-edit the location ID segment to an obviously-invalid value (e.g. `not-a-real-id`), then `browser_navigate` there directly. Verify: you land on that adventure's Locations page (not a blank screen or a stack trace), and a notification reading "Location not found or access denied: not-a-real-id" is visible. Repeat once more with an invalid adventure ID in any URL, verifying you land on the Adventures list with an "Adventure not found or access denied: ..." notification.

- [ ] **Step 4: Report**

Summarize: which of the 13 URLs were verified and confirmed working, the two cascading-forward checks and their outcomes, and the in-app regression walk's outcome. If anything failed, describe the exact URL, what was expected, and what actually rendered — do not fix it as part of this task.

---
