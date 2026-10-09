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
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.view.support.ViewSupporter;
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
        adventure.getLocationData().put("loc-1", new LocationData());
        adventure.setCurrentLocationId("loc-1");

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

    @SuppressWarnings("unchecked")
    @Test
    void runAdventureButton_staysDisabledWithATooltip_whenTheAdventureHasNoStartLocation() {
        adventure.setCurrentLocationId("");
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        test((Grid<AdventureData>) find(Grid.class, view).single()).select(0);

        Button runButton = find(Button.class, view).withText("Run Adventure").single();
        assertThat(runButton.isEnabled()).isFalse();
        assertThat(runButton.getTooltip().getText()).isEqualTo(ViewSupporter.NO_START_LOCATION_TEXT);
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

    // Same limitation as for delete: the context-menu click can't be driven, so the action
    // behind the "Duplicate" item (package-private duplicateAdventure) is called directly.
    @SuppressWarnings("unchecked")
    @Test
    void duplicate_addsTheCopyToTheGridAndAsksTheAccessServiceToCopyTheSelectedAdventure() {
        AdventureData copy = new AdventureData();
        copy.setId("adv-2");
        copy.setTitle("The Demo (copy)");
        when(accessService.duplicateAdventure(eq("adv-1"), any(UserData.class))).thenReturn(copy);
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        view.duplicateAdventure(adventure, grid);

        verify(accessService).duplicateAdventure(eq("adv-1"), any(UserData.class));
        assertThat(grid.getListDataView().getItems()).extracting(AdventureData::getId)
                                                       .containsExactly("adv-1", "adv-2");
    }

    @SuppressWarnings("unchecked")
    @Test
    void duplicate_whenTheCopyFails_leavesTheGridAsItWasAndDoesNotThrow() {
        when(accessService.duplicateAdventure(any(), any(UserData.class)))
                .thenThrow(new IllegalStateException("boom"));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        view.duplicateAdventure(adventure, grid);

        assertThat(grid.getListDataView().getItems()).extracting(AdventureData::getId).containsExactly("adv-1");
    }
}
