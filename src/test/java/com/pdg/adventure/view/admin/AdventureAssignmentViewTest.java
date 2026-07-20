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
        Grid<?> adventureGrid = find(Grid.class, view).atIndex(1);
        test(adventureGrid).select(0);
        return find(Grid.class, view).atIndex(2); // players grid appears in the detail panel
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

        Grid<?> adventureGrid = find(Grid.class, view).atIndex(1);
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
