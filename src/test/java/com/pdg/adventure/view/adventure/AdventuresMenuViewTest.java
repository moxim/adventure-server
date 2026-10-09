package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import com.pdg.adventure.server.exception.AdventureImportException;
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

    // The Upload component's own maximum is only checked by the browser; the server must refuse a larger body itself.
    @Test
    void importUploadHandler_refusesOnTheServerWhatIsLargerThanTheImportLimit() {
        UploadHandler handler = AdventuresMenuView.importUploadHandler(new AtomicReference<>(), 1234L);

        assertThat(handler.getFileSizeMax()).isEqualTo(1234L);
        assertThat(handler.getRequestSizeMax()).isEqualTo(1234L);
        assertThat(handler.getFileCountMax()).isEqualTo(1L);
    }

    @Test
    void importAdventureButton_isOnTheView() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        assertThat(find(Button.class, view).withText("Import Adventure").single().isEnabled()).isTrue();
    }

    @Test
    void importDialog_offersASingleJsonFileUploadAndStartsWithImportDisabled() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        Dialog dialog = view.buildImportDialog();
        UI.getCurrent().add(dialog);
        dialog.open();

        Upload upload = find(Upload.class, dialog).single();
        assertThat(upload.getMaxFiles()).isEqualTo(1);
        assertThat(upload.getAcceptedFileExtensions()).containsExactly(".json");
        assertThat(find(Button.class, dialog).withText("Import").single().isEnabled()).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void importAdventure_addsTheImportedAdventureToTheGridAndPassesTheFileOn() {
        AdventureData imported = new AdventureData();
        imported.setId("adv-9");
        imported.setTitle("From Elsewhere");
        byte[] file = {1, 2, 3};
        when(accessService.importAdventure(eq(file), any(UserData.class)))
                .thenReturn(new AdventureAccessService.ImportedAdventure(imported, false));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        boolean imports = view.importAdventure(file);

        assertThat(imports).isTrue();
        assertThat(grid.getListDataView().getItems()).extracting(AdventureData::getId)
                                                      .containsExactly("adv-1", "adv-9");
    }

    @SuppressWarnings("unchecked")
    @Test
    void importAdventure_whenTheFileIsRejected_leavesTheGridAsItWasAndDoesNotThrow() {
        when(accessService.importAdventure(any(), any(UserData.class)))
                .thenThrow(new AdventureImportException("This is not an adventure file."));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        boolean imports = view.importAdventure(new byte[] {1});

        assertThat(imports).isFalse();
        assertThat(grid.getListDataView().getItems()).extracting(AdventureData::getId).containsExactly("adv-1");
    }

    @Test
    void importAdventure_whenSomethingUnexpectedFails_returnsFalseInsteadOfThrowing() {
        when(accessService.importAdventure(any(), any(UserData.class))).thenThrow(new IllegalStateException("boom"));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        assertThat(view.importAdventure(new byte[] {1})).isFalse();
    }

    @Test
    void export_asksTheAccessServiceForTheFileAndPointsTheDownloadLinkAtIt() {
        when(accessService.exportAdventure(eq("adv-1"), any(UserData.class))).thenReturn(new byte[] {1, 2, 3});
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        view.exportAdventure(adventure);

        verify(accessService).exportAdventure(eq("adv-1"), any(UserData.class));
        assertThat(find(Anchor.class, view).single().getHref()).isNotBlank();
    }

    @Test
    void export_whenItIsRefused_offersNoDownloadAndDoesNotThrow() {
        when(accessService.exportAdventure(any(), any(UserData.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("no"));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        view.exportAdventure(adventure);

        assertThat(find(Anchor.class, view).single().getHref()).isNullOrEmpty();
    }

    @Test
    void exportFileName_turnsATitleIntoASafeFileName() {
        assertThat(AdventuresMenuView.exportFileName("The Demo")).isEqualTo("The-Demo.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("  ../etc/passwd  ")).isEqualTo("etc-passwd.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("Über \"Größe\": 1/2")).isEqualTo("Über-Größe-1-2.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("")).isEqualTo("adventure.adventure.json");
        assertThat(AdventuresMenuView.exportFileName(null)).isEqualTo("adventure.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("///")).isEqualTo("adventure.adventure.json");
    }
}
