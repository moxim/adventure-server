package com.pdg.adventure.view.admin;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Delete in the edit dialog only deletes after the ConfirmDialog is confirmed")
    void deleteUser_asksForConfirmation_deletesOnConfirm() {
        test(find(Grid.class, view).single()).doubleClickRow(1); // row 1 = paul

        Dialog editDialog = find(Dialog.class).single();
        test(find(Button.class, editDialog).withText("Delete").single()).click();

        verify(userService, never()).delete(anyString());

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        assertThat(test(confirm).getHeader()).isEqualTo("Delete User");
        assertThat(test(confirm).getText()).contains("paul");

        test(confirm).confirm();

        verify(userService).delete("paul-id");
        assertThat(find(Dialog.class).all()).as("edit dialog closed after delete").isEmpty();
        verify(userService, times(2)).findAll(); // initial + refresh
    }

    @Test
    @DisplayName("Cancelling the delete confirmation keeps the user")
    void deleteUser_cancelKeepsUser() {
        test(find(Grid.class, view).single()).doubleClickRow(1);

        Dialog editDialog = find(Dialog.class).single();
        test(find(Button.class, editDialog).withText("Delete").single()).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        test(confirm).cancel();

        verify(userService, never()).delete(anyString());
        assertThat(find(Dialog.class).all()).as("edit dialog still open").hasSize(1);
    }

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
}
