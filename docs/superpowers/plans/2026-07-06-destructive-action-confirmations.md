# Destructive-Action Confirmations (UX Audit P1.4) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every destructive action in the admin views (delete user, remove player) asks for confirmation before executing, the Delete button no longer appears when creating a new user, and an admin cannot delete their own account.

**Architecture:** Reuse the existing `ViewSupporter.getConfirmDialog(...)` builder (already used by `LocationsMenuView`) for the user-delete confirmation, and extract a small private `confirmRemoval(header, text, onConfirm)` helper inside `AdventureAssignmentView` so player-removal gains the same ConfirmDialog that author-removal already has. No new service APIs; all changes are view-layer. Tests are browserless UI-unit tests (`com.vaadin.browserless.BrowserlessTest`), which can open dialogs and click through ConfirmDialogs without a browser.

**Tech Stack:** Java 21+, Vaadin 25.2.1 (`ConfirmDialog`, `Notification`), `browserless-test-junit6` 1.1.1 (JUnit Jupiter), Mockito, AssertJ, Maven.

## Global Constraints

- Git repository root is `server/` — run ALL `git` and `mvn` commands from `/Users/mafw/workroom/projects/adventurebuilder/server`. There is no `.git` at the project root.
- Work on a feature branch (create worktree/branch at execution time per superpowers:using-git-worktrees), e.g. `destructive-action-confirmations`.
- Commit messages follow Conventional Commits (`feat:`, `test:`, `refactor:`) — the project's changelog automation parses them. End each commit message with the `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>` trailer.
- View tests extend `com.vaadin.browserless.BrowserlessTest` (NOT `SpringBrowserlessTest` — see `AGENTS-testing.md`). Services are plain `mock(...)`; no Spring context.
- `ViewSupporter.getCurrentUser()` reads `SecurityContextHolder` and throws without an `Authentication` — every test that constructs `AdventureAssignmentView` or opens the user edit dialog MUST populate the security context in `@BeforeEach` and clear it in `@AfterEach`.
- `ComboBox`/`MultiSelectComboBox` value operations are unreliable in browserless tests (no `DataKeyMapper`) — do not assert combo values; assert on mocks/model instead.
- Tester API (verified against `browserless-test-shared-1.1.1.jar`): `test(grid).doubleClickRow(int)`, `test(grid).select(int)`, `test(grid).getCellComponent(int row, int col)`, `test(confirmDialog).confirm()` / `.cancel()` / `.getHeader()` / `.getText()`, `test(notification).getText()`, `test(button).click()`.
- Component queries: `find(SomeComponent.class, view)` for children of the manually-added view; unscoped `find(Dialog.class)` / `find(ConfirmDialog.class)` / `find(Notification.class)` for overlays (they attach to the UI, not the view).
- Main-code parameter naming follows the project convention (`aHeader`, `anId`, `someRoles`).

**Out of scope (explicitly):** logout confirmation (already implemented on the `logout 2-step` work, 2026-07-01), save-toast sweep across editor views (UserManagementView and AdventureAssignmentView already show success/error notifications), referential-integrity guard for deleting users that are authors/players of adventures (follow-up; would need new `AdventureAccessService` queries), and the stale-build discrepancies noted in `docs/ux-audit-2026-07-06.md` (author-remove confirmation and toasts already exist in current source).

## File Structure

| File | Change |
|---|---|
| `src/main/java/com/pdg/adventure/view/admin/UserManagementView.java` | Modify: hide Delete in create mode (Task 1); ConfirmDialog before delete (Task 2); self-delete guard (Task 3) |
| `src/test/java/com/pdg/adventure/view/admin/TestUsers.java` | Create: shared `UserData` fixture builder for admin-view tests (Task 1) |
| `src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java` | Rewrite: convert to `BrowserlessTest` base, add dialog tests (Tasks 1–3) |
| `src/main/java/com/pdg/adventure/view/admin/AdventureAssignmentView.java` | Modify: extract `confirmRemoval` helper, confirm player removal (Task 4) |
| `src/test/java/com/pdg/adventure/view/admin/AdventureAssignmentViewTest.java` | Create: browserless tests for player/author removal confirmation (Task 4) |

All paths below are relative to `/Users/mafw/workroom/projects/adventurebuilder/server`.

---

### Task 1: Hide Delete button in the user CREATE dialog

The Create User dialog currently shows a Delete button that can never do anything (`user.getId() == null`). Hide it, and convert the existing test class to the browserless base so dialog interactions become testable.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/admin/UserManagementView.java:111-124`
- Create: `src/test/java/com/pdg/adventure/view/admin/TestUsers.java`
- Rewrite: `src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java`

**Interfaces:**
- Consumes: `ViewSupporter.doubleClickEditHint()` (unchanged), `UserService.findAll()`.
- Produces: shared fixture `TestUsers.user(String anId, String aName, Role... someRoles)` (package-private, same test package — Task 4's test class uses it too) and the `@BeforeEach`/`@AfterEach` security-context pattern that Tasks 2–3 extend. `deleteBtn` visibility rule: visible only when `user.getId() != null`.

- [ ] **Step 1: Create the shared fixture and rewrite the test class on the browserless base, with a failing create-dialog test**

Create `src/test/java/com/pdg/adventure/view/admin/TestUsers.java`:

```java
package com.pdg.adventure.view.admin;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;

/**
 * Builds UserData fixtures for admin-view tests. UserData has no setId
 * (JPA @PrePersist generates it), so the id is set via reflection to make
 * fixtures read as EXISTING users.
 */
final class TestUsers {

    private TestUsers() {
    }

    static UserData user(String anId, String aName, Role... someRoles) {
        try {
            UserData userData = new UserData();
            Field idField = UserData.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userData, anId);
            userData.setUsername(aName);
            userData.setPassword("irrelevant");
            userData.setRoles(new HashSet<>(Set.of(someRoles)));
            userData.setEnabled(true);
            return userData;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not build UserData fixture", e);
        }
    }
}
```

Replace the entire content of `src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java` with:

```java
package com.pdg.adventure.view.admin;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.UserService;

class UserManagementViewTest extends BrowserlessTest {

    private UserService userService;
    private UserManagementView view;
    private UserData admin;
    private UserData paul;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        admin = TestUsers.user("admin-id", "admin", Role.ADMIN, Role.AUTHOR, Role.PLAYER);
        paul = TestUsers.user("paul-id", "paul", Role.PLAYER);
        when(userService.findAll()).thenReturn(List.of(admin, paul));

        // ViewSupporter.getCurrentUser() reads the SecurityContext; the self-delete
        // guard (Task 3) evaluates it whenever the edit dialog opens.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));

        view = new UserManagementView(userService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void heading_isUserFriendly_doesNotLeakInternalClassName() {
        H2 heading = view.getChildren()
                .filter(H2.class::isInstance).map(H2.class::cast)
                .findFirst().orElseThrow();
        assertThat(heading.getText()).doesNotContain("UserData").contains("User");
    }

    @Test
    @DisplayName("Create User dialog shows no Delete button — there is nothing to delete yet")
    void createDialog_hasNoVisibleDeleteButton() {
        test(find(Button.class, view).withText("Add New User").single()).click();

        Dialog dialog = find(Dialog.class).single();
        List<Button> deleteButtons = find(Button.class, dialog).withText("Delete").all();
        boolean anyVisible = deleteButtons.stream().anyMatch(Button::isVisible);
        assertThat(anyVisible).as("Delete button visible in CREATE dialog").isFalse();
    }
}
```

- [ ] **Step 2: Run the tests to verify the new test fails**

Run (from `server/`): `mvn -Dstyle.color=never test -Dtest='UserManagementViewTest'`
Expected: `createDialog_hasNoVisibleDeleteButton` FAILS with `Delete button visible in CREATE dialog ... Expecting value to be false but was true`. The heading test PASSES.

- [ ] **Step 3: Hide the button in create mode**

In `src/main/java/com/pdg/adventure/view/admin/UserManagementView.java`, the delete button currently reads (lines 111–119):

```java
        Button deleteBtn = new Button("Delete", e -> {
            if (user.getId() != null) {
                userService.delete(user.getId());
                updateList();
                dialog.close();
                Notification notification = Notification.show("User deleted.", 2000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
```

Add one line directly after that block (before the `// If editing, ...` comment):

```java
        deleteBtn.setVisible(user.getId() != null);
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn -Dstyle.color=never test -Dtest='UserManagementViewTest'`
Expected: PASS (`Tests run: 2, Failures: 0`).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/admin/UserManagementView.java src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java
git commit -m "feat: hide delete button in user create dialog

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: ConfirmDialog before deleting a user

One click on Delete currently erases the user permanently. Route it through `ViewSupporter.getConfirmDialog(...)` — the same builder `LocationsMenuView` uses — so the admin must confirm first.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/admin/UserManagementView.java` (delete handler from Task 1 Step 3; imports)
- Modify: `src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java` (add two tests)

**Interfaces:**
- Consumes: `ViewSupporter.getConfirmDialog(String aHeader, String aType, String anId)` — returns a `ConfirmDialog` with header `aHeader`, text `"Are you sure you want to delete " + aType + " '" + anId + "'?"`, a red "Delete" confirm button, and Cancel enabled. Caller adds a confirm listener and calls `open()`. Also `UserService.delete(String id)`.
- Produces: delete flow = click Delete → ConfirmDialog opens → confirm runs `userService.delete(id)`, refreshes grid, closes the edit dialog, shows "User deleted." notification. Cancel leaves everything untouched. Task 3 modifies the same handler region.

- [ ] **Step 1: Write two failing tests**

Add to `UserManagementViewTest`:

```java
    @Test
    @DisplayName("Delete in the edit dialog only deletes after the ConfirmDialog is confirmed")
    void deleteUser_asksForConfirmation_deletesOnConfirm() {
        test(find(com.vaadin.flow.component.grid.Grid.class, view).single()).doubleClickRow(1); // row 1 = paul

        Dialog editDialog = find(Dialog.class).single();
        test(find(Button.class, editDialog).withText("Delete").single()).click();

        org.mockito.Mockito.verify(userService, org.mockito.Mockito.never()).delete(org.mockito.Mockito.anyString());

        com.vaadin.flow.component.confirmdialog.ConfirmDialog confirm =
                find(com.vaadin.flow.component.confirmdialog.ConfirmDialog.class).single();
        assertThat(test(confirm).getHeader()).isEqualTo("Delete User");
        assertThat(test(confirm).getText()).contains("paul");

        test(confirm).confirm();

        org.mockito.Mockito.verify(userService).delete("paul-id");
        assertThat(find(Dialog.class).all()).as("edit dialog closed after delete").isEmpty();
        org.mockito.Mockito.verify(userService, org.mockito.Mockito.times(2)).findAll(); // initial + refresh
    }

    @Test
    @DisplayName("Cancelling the delete confirmation keeps the user")
    void deleteUser_cancelKeepsUser() {
        test(find(com.vaadin.flow.component.grid.Grid.class, view).single()).doubleClickRow(1);

        Dialog editDialog = find(Dialog.class).single();
        test(find(Button.class, editDialog).withText("Delete").single()).click();

        com.vaadin.flow.component.confirmdialog.ConfirmDialog confirm =
                find(com.vaadin.flow.component.confirmdialog.ConfirmDialog.class).single();
        test(confirm).cancel();

        org.mockito.Mockito.verify(userService, org.mockito.Mockito.never()).delete(org.mockito.Mockito.anyString());
        assertThat(find(Dialog.class).all()).as("edit dialog still open").hasSize(1);
    }
```

Hoist the fully-qualified names into imports while adding them (`com.vaadin.flow.component.confirmdialog.ConfirmDialog`, `com.vaadin.flow.component.grid.Grid`, `static org.mockito.Mockito.*` additions: `verify`, `never`, `times`, `anyString`) — the code above is written fully-qualified only so this step is copy-pasteable in isolation.

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn -Dstyle.color=never test -Dtest='UserManagementViewTest'`
Expected: both new tests FAIL — `deleteUser_asksForConfirmation_deletesOnConfirm` because `delete` is invoked immediately (the `verify(..., never())` assertion trips), `deleteUser_cancelKeepsUser` because no `ConfirmDialog` exists (`single()` throws).

- [ ] **Step 3: Wrap the delete in a ConfirmDialog**

In `UserManagementView.java`, add the import:

```java
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
```

Replace the delete button block (from Task 1 Step 3, including the added visibility line):

```java
        Button deleteBtn = new Button("Delete", e -> {
            if (user.getId() != null) {
                userService.delete(user.getId());
                updateList();
                dialog.close();
                Notification notification = Notification.show("User deleted.", 2000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        deleteBtn.setVisible(user.getId() != null);
```

with:

```java
        Button deleteBtn = new Button("Delete", e -> {
            ConfirmDialog confirm = ViewSupporter.getConfirmDialog("Delete User", "user", user.getUsername());
            confirm.addConfirmListener(_ -> {
                userService.delete(user.getId());
                updateList();
                dialog.close();
                Notification notification = Notification.show("User deleted.", 2000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            confirm.open();
        });
        deleteBtn.setVisible(user.getId() != null);
```

(The `if (user.getId() != null)` guard is no longer needed — the button is invisible in create mode.)

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn -Dstyle.color=never test -Dtest='UserManagementViewTest'`
Expected: PASS (`Tests run: 4, Failures: 0`).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/admin/UserManagementView.java src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java
git commit -m "feat: require confirmation before deleting a user

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: Prevent self-deletion

The code already carries a comment wishing for this guard. An admin editing their own account gets a disabled Delete button with a tooltip explaining why.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/admin/UserManagementView.java` (delete button region from Task 2; imports)
- Modify: `src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java` (add two tests)

**Interfaces:**
- Consumes: `ViewSupporter.getCurrentUser()` → returns the authenticated `UserData` or throws `IllegalStateException` (SecurityContext is populated by the shared `@BeforeEach` from Task 1).
- Produces: in edit mode, `deleteBtn.isEnabled()` is `false` and tooltip text is `"You cannot delete your own account."` when editing the logged-in user; unchanged behavior for other users and create mode (guard only evaluated when `user.getId() != null`, so `getCurrentUser()` is never called for the create dialog).

- [ ] **Step 1: Write two failing tests**

Add to `UserManagementViewTest`:

```java
    @Test
    @DisplayName("Editing your own account disables Delete with an explanatory tooltip")
    void editOwnAccount_deleteIsDisabled() {
        test(find(Grid.class, view).single()).doubleClickRow(0); // row 0 = admin = current user

        Dialog editDialog = find(Dialog.class).single();
        Button deleteBtn = find(Button.class, editDialog).withText("Delete").single();

        assertThat(deleteBtn.isEnabled()).as("Delete enabled on own account").isFalse();
        assertThat(deleteBtn.getTooltip().getText()).isEqualTo("You cannot delete your own account.");
    }

    @Test
    @DisplayName("Editing another user keeps Delete enabled")
    void editOtherUser_deleteIsEnabled() {
        test(find(Grid.class, view).single()).doubleClickRow(1); // row 1 = paul

        Dialog editDialog = find(Dialog.class).single();
        Button deleteBtn = find(Button.class, editDialog).withText("Delete").single();

        assertThat(deleteBtn.isEnabled()).isTrue();
    }
```

- [ ] **Step 2: Run the tests to verify the guard test fails**

Run: `mvn -Dstyle.color=never test -Dtest='UserManagementViewTest'`
Expected: `editOwnAccount_deleteIsDisabled` FAILS (`Delete enabled on own account ... expecting false but was true`); `editOtherUser_deleteIsEnabled` PASSES.

- [ ] **Step 3: Add the guard**

In `UserManagementView.java`, directly after the `deleteBtn.setVisible(user.getId() != null);` line from Task 2, add:

```java
        if (user.getId() != null
                && user.getUsername().equals(ViewSupporter.getCurrentUser().getUsername())) {
            deleteBtn.setEnabled(false);
            deleteBtn.setTooltipText("You cannot delete your own account.");
        }
```

Also delete the now-fulfilled wish comment two lines below:

```java
        // If editing, you might want to disable the delete button for the current admin user
        // to prevent them from deleting themselves.
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn -Dstyle.color=never test -Dtest='UserManagementViewTest'`
Expected: PASS (`Tests run: 6, Failures: 0`).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/admin/UserManagementView.java src/test/java/com/pdg/adventure/view/admin/UserManagementViewTest.java
git commit -m "feat: prevent admins from deleting their own account

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 4: ConfirmDialog before removing a player from an adventure

Author-removal already confirms; player-removal doesn't. Extract the dialog construction into a private `confirmRemoval` helper, refactor `confirmRemoveAuthor` onto it, and give player-removal the same treatment. Finish with the full test suite.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/admin/AdventureAssignmentView.java:173-184` (confirmRemoveAuthor), `:241-246` (players-grid Remove listener), new private methods
- Create: `src/test/java/com/pdg/adventure/view/admin/AdventureAssignmentViewTest.java`

**Interfaces:**
- Consumes: `AdventureAccessService` mocks — `getAuthorNamesByAdventureId(): Map<String,String>`, `getAdventuresForUser(UserData): List<AdventureData>`, `findAuthorForAdventure(String): Optional<UserData>`, `findPlayersForAdventure(String): List<UserData>`, `removePlayer(String, UserData)`, `removeAuthor(String)`; `UserService.findByRole(Role): List<UserData>`; `AdventureData.setId(String)` / `setTitle(String)`; the shared `TestUsers.user(...)` fixture created in Task 1 (same test package).
- Produces: `private void confirmRemoval(String aHeader, String aText, Runnable anOnConfirm)` in `AdventureAssignmentView` — builds a cancelable ConfirmDialog with red "Remove" confirm button and opens it; `confirmRemoveAuthor` and new `confirmRemovePlayer` both delegate to it. `doRemovePlayer` / `doRemoveAuthor` unchanged.

- [ ] **Step 1: Create the failing test class**

Create `src/test/java/com/pdg/adventure/view/admin/AdventureAssignmentViewTest.java`:

```java
package com.pdg.adventure.view.admin;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.security.service.UserService;

class AdventureAssignmentViewTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private UserService userService;
    private AdventureAssignmentView view;
    private AdventureData adventure;
    private UserData admin;
    private UserData paul;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
        userService = mock(UserService.class);

        admin = TestUsers.user("admin-id", "admin", Role.ADMIN, Role.AUTHOR, Role.PLAYER);
        paul = TestUsers.user("paul-id", "paul", Role.PLAYER);

        adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");

        when(accessService.getAuthorNamesByAdventureId()).thenReturn(new HashMap<>());
        when(accessService.getAdventuresForUser(any())).thenReturn(List.of(adventure));
        when(accessService.findAuthorForAdventure("adv-1")).thenReturn(Optional.empty());
        when(accessService.findPlayersForAdventure("adv-1")).thenReturn(List.of(paul));
        when(userService.findByRole(Role.PLAYER)).thenReturn(List.of(paul));
        when(userService.findByRole(Role.AUTHOR)).thenReturn(List.of(admin));

        // The view constructor calls ViewSupporter.getCurrentUser()
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));

        view = new AdventureAssignmentView(accessService, userService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Grid<?> selectAdventureAndGetPlayersGrid() {
        Grid<?> adventureGrid = find(Grid.class, view).atIndex(0);
        test(adventureGrid).select(0);
        return find(Grid.class, view).atIndex(1); // players grid appears in the detail panel
    }

    @Test
    @DisplayName("Removing a player only executes after the ConfirmDialog is confirmed")
    void removePlayer_asksForConfirmation_removesOnConfirm() {
        Grid<?> playersGrid = selectAdventureAndGetPlayersGrid();

        Button removeBtn = (Button) test(playersGrid).getCellComponent(0, 1);
        test(removeBtn).click();

        verify(accessService, never()).removePlayer(anyString(), any());

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        assertThat(test(confirm).getHeader()).isEqualTo("Remove Player");
        assertThat(test(confirm).getText()).contains("paul").contains("The Demo");

        test(confirm).confirm();

        verify(accessService).removePlayer("adv-1", paul);
        Notification notification = find(Notification.class).first();
        assertThat(test(notification).getText()).contains("Player removed.");
    }

    @Test
    @DisplayName("Cancelling the player-removal confirmation keeps the player")
    void removePlayer_cancelKeepsPlayer() {
        Grid<?> playersGrid = selectAdventureAndGetPlayersGrid();

        Button removeBtn = (Button) test(playersGrid).getCellComponent(0, 1);
        test(removeBtn).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        test(confirm).cancel();

        verify(accessService, never()).removePlayer(anyString(), any());
    }

    @Test
    @DisplayName("Author removal still asks for confirmation after the helper refactor")
    void removeAuthor_stillAsksForConfirmation() {
        // Re-stub: an author is assigned and there are NO players, so the only
        // "Remove" button on screen belongs to the author row.
        when(accessService.findAuthorForAdventure("adv-1")).thenReturn(Optional.of(admin));
        when(accessService.findPlayersForAdventure("adv-1")).thenReturn(List.of());

        Grid<?> adventureGrid = find(Grid.class, view).atIndex(0);
        test(adventureGrid).select(0);

        Button removeBtn = find(Button.class, view).withText("Remove").single();
        test(removeBtn).click();

        verify(accessService, never()).removeAuthor(anyString());

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        assertThat(test(confirm).getHeader()).isEqualTo("Remove Author");

        test(confirm).confirm();

        verify(accessService).removeAuthor("adv-1");
    }
}
```

- [ ] **Step 2: Run the tests to verify the player tests fail**

Run: `mvn -Dstyle.color=never test -Dtest='AdventureAssignmentViewTest'`
Expected: `removePlayer_asksForConfirmation_removesOnConfirm` FAILS at `verify(accessService, never()).removePlayer(...)` (removal happens immediately); `removePlayer_cancelKeepsPlayer` FAILS at `find(ConfirmDialog.class).single()` (no dialog opens). `removeAuthor_stillAsksForConfirmation` PASSES (behavior already exists).

- [ ] **Step 3: Extract the helper and confirm player removal**

In `src/main/java/com/pdg/adventure/view/admin/AdventureAssignmentView.java`:

Replace `confirmRemoveAuthor` (lines 173–184):

```java
    private void confirmRemoveAuthor(UserData author) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Remove Author");
        dialog.setText("Remove " + author.getUsername() + " as author of \""
                + selectedAdventure.getTitle() + "\"?");
        dialog.setConfirmText("Remove");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.addConfirmListener(_ -> doRemoveAuthor());
        dialog.open();
    }
```

with:

```java
    private void confirmRemoveAuthor(UserData author) {
        confirmRemoval("Remove Author",
                "Remove " + author.getUsername() + " as author of \""
                        + selectedAdventure.getTitle() + "\"?",
                this::doRemoveAuthor);
    }

    private void confirmRemovePlayer(UserData player) {
        confirmRemoval("Remove Player",
                "Remove " + player.getUsername() + " as player of \""
                        + selectedAdventure.getTitle() + "\"?",
                () -> doRemovePlayer(player));
    }

    private void confirmRemoval(final String aHeader, final String aText, final Runnable anOnConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(aHeader);
        dialog.setText(aText);
        dialog.setConfirmText("Remove");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.addConfirmListener(_ -> anOnConfirm.run());
        dialog.open();
    }
```

In `buildPlayersSection()` (line 244), change the players-grid remove listener from:

```java
            remove.addClickListener(_ -> doRemovePlayer(player));
```

to:

```java
            remove.addClickListener(_ -> confirmRemovePlayer(player));
```

- [ ] **Step 4: Run the view tests to verify they pass**

Run: `mvn -Dstyle.color=never test -Dtest='AdventureAssignmentViewTest'`
Expected: PASS (`Tests run: 3, Failures: 0`).

- [ ] **Step 5: Run the FULL test suite (AGENTS-testing.md requires this after multi-file changes)**

Run: `mvn -Dstyle.color=never test`
Expected: `BUILD SUCCESS`, zero failures/errors. Note the total test count in the task report (baseline was 808 before this plan; expect that plus the 9 new tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/admin/AdventureAssignmentView.java src/test/java/com/pdg/adventure/view/admin/AdventureAssignmentViewTest.java
git commit -m "feat: require confirmation before removing a player from an adventure

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

## Verification Notes for the Reviewer

- Manual smoke test (optional, needs running app + MySQL/Mongo): log in as `admin`/`admin123` → Dashboard → User Management → create a throwaway user (no Delete button in the create dialog) → double-click it → Delete → confirmation appears → Confirm → "User deleted." toast. Then Game Assignments → select The Demo → Remove on a player → confirmation appears → Cancel keeps the player.
- The audit document `docs/ux-audit-2026-07-06.md` (project root, NOT server/docs) describes P1.4 against a stale running build; this plan targets what is actually missing in current source. After merging, re-run the app before re-auditing.
