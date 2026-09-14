package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
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

class AdventuresMenuViewTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureData adventure;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);

        adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        when(accessService.getAdventuresForUser(any(UserData.class)))
                .thenReturn(new ArrayList<>(List.of(adventure)));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unchecked")
    @Test
    void runAdventureButton_disabledUntilARowIsSelected() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        Button runButton = find(Button.class, view).withText("Run Adventure").single();
        assertThat(runButton.isEnabled()).isFalse();

        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();
        test(grid).select(0);

        assertThat(runButton.isEnabled()).isTrue();
    }

    // Delete goes through a GridContextMenu item click, which has no reliable way to be driven
    // from a browserless test - these tests instead build the confirmation dialog directly
    // (buildDeleteConfirmDialog is package-private for exactly this) and drive it as a user
    // would: open it, then confirm or cancel.
    @SuppressWarnings("unchecked")
    @Test
    void deleteConfirmDialog_showsTheAdventuresTitleAndDoesNotDeleteUntilConfirmed() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(adventure, grid);
        UI.getCurrent().add(dialog);
        dialog.open();

        assertThat(test(dialog).getHeader()).isEqualTo("Delete Adventure");
        assertThat(test(dialog).getText()).contains("The Demo");
        verify(accessService, never()).deleteAdventure(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void confirmingDeleteDialog_removesTheAdventureAndCallsAccessService() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(adventure, grid);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).confirm();

        verify(accessService).deleteAdventure(eq("adv-1"), any(UserData.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void cancelingDeleteDialog_deletesNothing() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(adventure, grid);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).cancel();

        verify(accessService, never()).deleteAdventure(any(), any());
    }
}
