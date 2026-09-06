package com.pdg.adventure.view.systemmessage;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class SystemMessagesViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private SystemMessagesView view;
    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);

        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setTitle("Test Adventure");
        // Left empty on purpose - storage is sparse, so an adventure with no customizations at
        // all has an empty systemMessages map; the grid must still show all 36 catalog rows.

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        view = new SystemMessagesView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithAdventure() {
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
    }

    @Test
    @DisplayName("Entering the view does NOT materialize rows for unmodified messages - storage stays sparse")
    void beforeEnter_leavesEmptyMapEmpty_forAnAdventureWithNoCustomizations() {
        enterWithAdventure();

        assertThat(adventureData.getSystemMessages()).as("no rows should be created just by viewing the screen").isEmpty();
    }

    @Test
    @DisplayName("The grid still shows all 65 catalog rows even though none are persisted yet")
    void grid_showsFullCatalog_regardlessOfHowManyRowsArePersisted() {
        enterWithAdventure();

        assertThat(test(find(Grid.class, view).single()).size()).isEqualTo(65);
    }

    @Test
    @DisplayName("Entering the view never touches an already-customized message, and doesn't materialize the other 35")
    void beforeEnter_preservesAlreadyEditedMessage_andLeavesEverythingElseSparse() {
        adventureData.getSystemMessages().put(SystemMessageKey.SM40.id(),
                new SystemMessageData("adv-1", SystemMessageKey.SM40.id(), "Du kannst %s nicht tragen."));

        enterWithAdventure();

        assertThat(adventureData.getSystemMessages()).hasSize(1);
        assertThat(adventureData.getSystemMessages().get(SystemMessageKey.SM40.id()).getText())
                .isEqualTo("Du kannst %s nicht tragen.");
    }

    @Test
    @DisplayName("Unknown adventure id forwards to the adventures menu instead of crashing")
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class))).thenReturn(Optional.empty());
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing")));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    @DisplayName("The view never shows a Create/Add or Delete button anywhere - the catalog is fixed")
    void view_hasNoCreateOrDeleteButtons() {
        enterWithAdventure();

        assertThat(find(Button.class, view).withText("Create").all()).isEmpty();
        assertThat(find(Button.class, view).withText("Add").all()).isEmpty();
        assertThat(find(Button.class, view).withText("Delete").all()).isEmpty();
    }

    @Test
    @DisplayName("Double-clicking a row opens an edit dialog populated with its current text")
    void doubleClick_opensEditDialogWithCurrentText() {
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.SM40.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);

        Dialog dialog = find(Dialog.class).single();
        assertThat(find(TextArea.class, dialog).single().getValue())
                .isEqualTo(SystemMessageKey.SM40.defaultText());
        assertThat(find(Button.class, dialog).withText("Delete").all()).isEmpty();
    }

    @Test
    @DisplayName("The edit dialog uses STRICT modality so ESC can't also trigger the background Back button")
    void editDialog_usesStrictModality() {
        // Regression test: this dialog previously left modality unset (defaults to VISUAL), which
        // doesn't mark the view inert - pressing ESC to close the dialog also fired the Back
        // button's global Key.ESCAPE shortcut, navigating to AdventureEditorView in the background
        // while the dialog itself stayed open.
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.SM40.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);

        Dialog dialog = find(Dialog.class).single();
        assertThat(dialog.getModality()).isEqualTo(ModalityMode.STRICT);
    }

    @Test
    @DisplayName("Saving a valid edit for a previously-unmodified message creates its first override row (sparse insert)")
    void save_validEdit_insertsFirstOverrideRow_mutatesAdventureDataAndSaves() {
        enterWithAdventure();
        assertThat(adventureData.getSystemMessages()).as("nothing customized yet").isEmpty();

        int wearRowIndex = gridIndexOf(SystemMessageKey.SM40.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);
        Dialog dialog = find(Dialog.class).single();
        TextArea textArea = find(TextArea.class, dialog).single();
        textArea.setValue("Du kannst %s nicht tragen.");
        test(find(Button.class, dialog).withText("Save").single()).click();

        // Exactly one row now exists - the other 35 unmodified keys are still not persisted.
        assertThat(adventureData.getSystemMessages()).hasSize(1);
        assertThat(adventureData.getSystemMessages().get(SystemMessageKey.SM40.id()).getText())
                .isEqualTo("Du kannst %s nicht tragen.");
        verify(adventureService).saveAdventureData(adventureData);
        assertThat(find(Dialog.class).all()).as("dialog closed after a successful save").isEmpty();
    }

    @Test
    @DisplayName("Saving a second edit for an already-customized message updates the existing row instead of duplicating it")
    void save_validEdit_updatesExistingOverrideRow_whenOneAlreadyExists() {
        adventureData.getSystemMessages().put(SystemMessageKey.SM40.id(),
                new SystemMessageData("adv-1", SystemMessageKey.SM40.id(), "Du kannst %s nicht tragen."));
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.SM40.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);
        Dialog dialog = find(Dialog.class).single();
        TextArea textArea = find(TextArea.class, dialog).single();
        textArea.setValue("Sie können %s nicht tragen.");
        test(find(Button.class, dialog).withText("Save").single()).click();

        assertThat(adventureData.getSystemMessages()).hasSize(1);
        assertThat(adventureData.getSystemMessages().get(SystemMessageKey.SM40.id()).getText())
                .isEqualTo("Sie können %s nicht tragen.");
    }

    @Test
    @DisplayName("Saving text with the wrong placeholders shows an error, leaves the dialog open, and never saves")
    void save_rejectedEdit_showsErrorAndDoesNotSave() {
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.SM40.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);
        Dialog dialog = find(Dialog.class).single();
        TextArea textArea = find(TextArea.class, dialog).single();
        textArea.setValue("You can't wear that.");
        test(find(Button.class, dialog).withText("Save").single()).click();

        verify(adventureService, never()).saveAdventureData(any());
        assertThat(find(Dialog.class).all()).as("dialog stays open after a rejected save").hasSize(1);
    }

    @Test
    @DisplayName("Saving blank text is rejected even for a message with no placeholders")
    void save_rejectsBlankText() {
        enterWithAdventure();

        int doneRowIndex = gridIndexOf(SystemMessageKey.SM15.id());
        test(find(Grid.class, view).single()).doubleClickRow(doneRowIndex);
        Dialog dialog = find(Dialog.class).single();
        TextArea textArea = find(TextArea.class, dialog).single();
        textArea.setValue("   ");
        test(find(Button.class, dialog).withText("Save").single()).click();

        verify(adventureService, never()).saveAdventureData(any());
        assertThat(find(Dialog.class).all()).hasSize(1);
    }

    @Test
    @DisplayName("Cancel closes the dialog without saving")
    void cancel_closesDialogWithoutSaving() {
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.SM40.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);
        Dialog dialog = find(Dialog.class).single();
        test(find(Button.class, dialog).withText("Cancel").single()).click();

        verify(adventureService, never()).saveAdventureData(any());
        assertThat(find(Dialog.class).all()).isEmpty();
    }

    // The grid is ordered by SystemMessageKey declaration order (see SystemMessagesView.updateList),
    // so tests locate a row by key instead of hardcoding a fragile positional index.
    private int gridIndexOf(String aKey) {
        SystemMessageKey[] keys = SystemMessageKey.values();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].id().equals(aKey)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown key: " + aKey);
    }
}
