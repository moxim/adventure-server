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
