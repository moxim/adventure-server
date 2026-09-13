package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Covers VocabularyMenuView interactions not already exercised by VocabularyMenuViewTest
 * (plain construction/beforeEnter) or VocabularyMenuViewRoutingTest (Create Word, routing).
 * Context-menu (Edit/Show Usages/Delete) flows are left uncovered here: there's no established
 * pattern in this codebase for driving a GridContextMenu item click in browserless tests, and
 * WordUsageTrackerTest already covers the usage-lookup logic those menu items delegate to.
 */
class VocabularyMenuViewBrowserlessTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private VocabularyMenuView view;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);

        vocabularyData = new VocabularyData();
        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setVocabularyData(vocabularyData);

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

    private void enterWithAdventure() {
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        var event = mock(com.vaadin.flow.router.BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new com.vaadin.flow.router.RouteParameters(
                new com.vaadin.flow.router.RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
    }

    @Test
    @DisplayName("Selecting a grid row enables the Edit Word button; clearing the selection disables it again")
    void selectingAGridRow_enablesEditButton() {
        vocabularyData.addWord(new Word("sword", Word.Type.NOUN));
        enterWithAdventure();

        Button editButton = find(Button.class, view).withText("Edit Word").single();
        assertThat(editButton.isEnabled()).isFalse();

        Grid<?> grid = find(Grid.class, view).single();
        test(grid).select(0);
        assertThat(editButton.isEnabled()).isTrue();

        grid.deselectAll();
        assertThat(editButton.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Double-clicking a grid row opens the word editor dialog pre-filled with that word")
    void doubleClickingAGridRow_opensEditDialogForThatWord() {
        vocabularyData.addWord(new Word("sword", Word.Type.NOUN));
        enterWithAdventure();

        Grid<?> grid = find(Grid.class, view).single();
        test(grid).doubleClickRow(0);

        Dialog dialog = find(Dialog.class).single();
        assertThat(find(TextField.class, dialog).single().getValue()).isEqualTo("sword");
    }

    @Test
    @DisplayName("Clicking Save delegates to AdventureService.saveAdventureData and shows a confirmation")
    void clickingSave_callsAdventureServiceSaveAndShowsNotification() {
        enterWithAdventure();

        test(find(Button.class, view).withText("Save").single()).click();

        verify(adventureService).saveAdventureData(adventureData);
        assertThat(find(Notification.class).single()).isNotNull();
    }

    @Test
    @DisplayName("Typing in the search field filters the grid down to matching words")
    void typingInSearchField_filtersGrid() {
        vocabularyData.addWord(new Word("sword", Word.Type.NOUN));
        vocabularyData.addWord(new Word("shield", Word.Type.NOUN));
        vocabularyData.addWord(new Word("golden", Word.Type.ADJECTIVE));
        enterWithAdventure();

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(3);

        TextField searchField = find(TextField.class, view).single();
        searchField.setValue("sh");

        assertThat(test(grid).size()).isEqualTo(1);
    }
}
