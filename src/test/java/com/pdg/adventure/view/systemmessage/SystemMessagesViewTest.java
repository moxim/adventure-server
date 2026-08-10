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
        // Left empty on purpose - beforeEnter's seedMissingInto call must top it up to all 36.

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
    @DisplayName("Entering the view seeds every missing catalog key into the adventure's (empty) system messages map")
    void beforeEnter_seedsMissingSystemMessages() {
        enterWithAdventure();

        assertThat(adventureData.getSystemMessages()).hasSize(36);
        assertThat(adventureData.getSystemMessages().get(SystemMessageKey.CANNOT_WEAR.id()).getText())
                .isEqualTo(SystemMessageKey.CANNOT_WEAR.defaultText());
    }

    @Test
    @DisplayName("Entering the view never overwrites an already-edited message")
    void beforeEnter_preservesAlreadyEditedMessage() {
        adventureData.getSystemMessages().put(SystemMessageKey.CANNOT_WEAR.id(),
                new SystemMessageData("adv-1", SystemMessageKey.CANNOT_WEAR.id(), "Du kannst %s nicht tragen."));

        enterWithAdventure();

        assertThat(adventureData.getSystemMessages().get(SystemMessageKey.CANNOT_WEAR.id()).getText())
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

        int wearRowIndex = gridIndexOf(SystemMessageKey.CANNOT_WEAR.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);

        Dialog dialog = find(Dialog.class).single();
        assertThat(find(TextArea.class, dialog).single().getValue())
                .isEqualTo(SystemMessageKey.CANNOT_WEAR.defaultText());
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

        int wearRowIndex = gridIndexOf(SystemMessageKey.CANNOT_WEAR.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);

        Dialog dialog = find(Dialog.class).single();
        assertThat(dialog.getModality()).isEqualTo(ModalityMode.STRICT);
    }

    @Test
    @DisplayName("Saving a valid edit mutates the adventure's system messages and persists via AdventureService")
    void save_validEdit_mutatesAdventureDataAndSaves() {
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.CANNOT_WEAR.id());
        test(find(Grid.class, view).single()).doubleClickRow(wearRowIndex);
        Dialog dialog = find(Dialog.class).single();
        TextArea textArea = find(TextArea.class, dialog).single();
        textArea.setValue("Du kannst %s nicht tragen.");
        test(find(Button.class, dialog).withText("Save").single()).click();

        assertThat(adventureData.getSystemMessages().get(SystemMessageKey.CANNOT_WEAR.id()).getText())
                .isEqualTo("Du kannst %s nicht tragen.");
        verify(adventureService).saveAdventureData(adventureData);
        assertThat(find(Dialog.class).all()).as("dialog closed after a successful save").isEmpty();
    }

    @Test
    @DisplayName("Saving text with the wrong placeholders shows an error, leaves the dialog open, and never saves")
    void save_rejectedEdit_showsErrorAndDoesNotSave() {
        enterWithAdventure();

        int wearRowIndex = gridIndexOf(SystemMessageKey.CANNOT_WEAR.id());
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

        int doneRowIndex = gridIndexOf(SystemMessageKey.GAMELOOP_DONE.id());
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

        int wearRowIndex = gridIndexOf(SystemMessageKey.CANNOT_WEAR.id());
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
