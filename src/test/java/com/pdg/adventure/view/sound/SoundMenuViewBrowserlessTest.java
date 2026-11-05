package com.pdg.adventure.view.sound;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.PlayActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.SoundService;
import com.pdg.adventure.view.support.RouteIds;

class SoundMenuViewBrowserlessTest extends BrowserlessTest {

    private SoundService soundService;
    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private SoundData tavern;
    private SoundData pirateBay;
    private SoundMenuView view;

    @BeforeEach
    void setUp() {
        soundService = mock(SoundService.class);
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        tavern = sound("snd-1", "tavern", 2048);
        pirateBay = sound("snd-2", "Pirate_Bay", 5L * 1024 * 1024 / 2);
        adventureData.getSoundData().put(tavern.getId(), tavern);
        adventureData.getSoundData().put(pirateBay.getId(), pirateBay);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view = new SoundMenuView(soundService, adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static SoundData sound(String id, String name, long sizeBytes) {
        SoundData sound = new SoundData();
        sound.setId(id);
        sound.setName(name);
        sound.setSizeBytes(sizeBytes);
        return sound;
    }

    private void enter() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        view.beforeEnter(event);
    }

    private void usedByAPlayInTheWorkflow(String aSoundId) {
        WorkflowData workflow = new WorkflowData();
        PlayActionData play = new PlayActionData();
        play.setSoundId(aSoundId);
        play.setTimes(1);
        CommandData command = new CommandData(new CommandDescriptionData(new Word("tick", Word.Type.VERB), null, null));
        command.addAction(play);
        workflow.getArrivalProcesses().add(command);
        adventureData.setWorkflowData(workflow);
    }

    @Test
    void grid_listsTheAdventuresSounds_withNameSizeAndUsedCount() {
        usedByAPlayInTheWorkflow("snd-1");
        enter();

        Grid<?> grid = find(Grid.class, view).single();

        assertThat(test(grid).size()).isEqualTo(2);
        // the Name column sorts; find each sound's row by its name instead of relying on the initial order
        for (int row = 0; row < 2; row++) {
            String name = test(grid).getCellText(row, 1);
            if (name.equals("tavern")) {
                assertThat(test(grid).getCellText(row, 2)).isEqualTo("2.0 KB");
                assertThat(test(grid).getCellText(row, 3)).isEqualTo("1");
            } else {
                assertThat(name).isEqualTo("Pirate_Bay");
                assertThat(test(grid).getCellText(row, 2)).isEqualTo("2.5 MB");
                assertThat(test(grid).getCellText(row, 3)).isEqualTo("0");
            }
        }
    }

    @Test
    void countLabel_showsTheNumberOfSounds() {
        enter();

        assertThat(find(Span.class, view).all()).extracting(Span::getText).contains("Sounds: 2");
    }

    @Test
    void playButtonIsPresentForEveryRow() {
        enter();
        Grid<?> grid = find(Grid.class, view).single();

        assertThat(test(grid).getCellComponent(0, 0)).isInstanceOf(SoundPlayButton.class);
        assertThat(test(grid).getCellComponent(1, 0)).isInstanceOf(SoundPlayButton.class);
    }

    @Test
    void search_filtersByNameIgnoringCase() {
        enter();

        test(find(TextField.class, view).single()).setValue("PIRATE");

        assertThat(test(find(Grid.class, view).single()).size()).isEqualTo(1);
    }

    @Test
    void editButton_isDisabled_untilASoundIsSelected() {
        enter();

        Button edit = find(Button.class, view).withText("Edit Sound").single();
        assertThat(edit.isEnabled()).isFalse();

        test(find(Grid.class, view).single()).select(0);
        assertThat(edit.isEnabled()).isTrue();
    }

    @Test
    void delete_ofAnUnusedSound_removesItFromTheAdventureAndTheStore() {
        enter();

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(tavern);
        UI.getCurrent().add(dialog);
        dialog.open();
        test(dialog).confirm();

        assertThat(adventureData.getSoundData()).doesNotContainKey("snd-1").containsKey("snd-2");
        verify(adventureService).saveAdventureData(adventureData);
        verify(soundService).delete("snd-1");
    }

    @Test
    void delete_ofASoundUsedByAPlayInAnArrivalProcess_isRefusedAndShowsUsage() {
        usedByAPlayInTheWorkflow("snd-1");
        enter();

        view.confirmDeleteSound(tavern);

        assertThat(find(ConfirmDialog.class).single()).isNotNull();
        assertThat(adventureData.getSoundData()).containsKey("snd-1");
        verify(soundService, never()).delete(any());
    }
}
