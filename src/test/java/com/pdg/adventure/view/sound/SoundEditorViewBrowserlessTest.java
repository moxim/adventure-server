package com.pdg.adventure.view.sound;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.streams.UploadEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
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
import com.pdg.adventure.model.SoundContentData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.action.PlayActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.SoundService;
import com.pdg.adventure.view.support.RouteIds;

class SoundEditorViewBrowserlessTest extends BrowserlessTest {

    private SoundService soundService;
    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private SoundData soundData;
    private SoundEditorView view;

    @BeforeEach
    void setUp() {
        soundService = mock(SoundService.class);
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        soundData = new SoundData();
        soundData.setId("snd-1");
        soundData.setName("pirate_bay");
        SoundContentData content = new SoundContentData();
        content.setId("c-1");
        content.setContent(new byte[] {1, 2, 3});
        soundData.setContent(content);
        adventureData.getSoundData().put(soundData.getId(), soundData);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view = new SoundEditorView(soundService, adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithSoundId(String soundId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        when(params.get(RouteIds.SOUND_ID.getValue())).thenReturn(Optional.ofNullable(soundId));
        view.beforeEnter(event);
    }

    private UploadEvent uploadEventWith(byte[] someBytes) {
        UploadEvent event = mock(UploadEvent.class);
        when(event.getInputStream()).thenReturn(new ByteArrayInputStream(someBytes));
        when(event.getUI()).thenReturn(UI.getCurrent());
        return event;
    }

    private static byte[] mp3Bytes(int aTotalLength) {
        byte[] bytes = new byte[aTotalLength];
        System.arraycopy(new byte[] {'I', 'D', '3', 4, 0, 0}, 0, bytes, 0, 6);
        return bytes;
    }

    private Button saveButton() {
        return find(Button.class, view).withText("Save").single();
    }

    private TextField nameField() {
        return find(TextField.class, view).all().getFirst();
    }

    @Test
    void saveButton_isDisabled_afterSetData() {
        enterWithSoundId(null);

        assertThat(saveButton().isEnabled()).isFalse();
    }

    @Test
    void pageTitles_distinguishNewAndEdit() {
        enterWithSoundId("snd-1");
        assertThat(view.getPageTitle()).isEqualTo("Edit Sound: pirate_bay");
        enterWithSoundId(null);
        assertThat(view.getPageTitle()).isEqualTo("New Sound");
    }

    @Test
    void handleUpload_ofAPngNamedMp3_isRefusedWithANotification() {
        enterWithSoundId(null);
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

        view.handleUpload(uploadEventWith(png));

        assertThat(test(find(Notification.class).single()).getText())
                .isEqualTo("The uploaded file is not a valid mp3 file.");
    }

    @Test
    void handleUpload_ofAnEmptyFile_isRefused() {
        enterWithSoundId(null);

        view.handleUpload(uploadEventWith(new byte[0]));

        assertThat(test(find(Notification.class).single()).getText())
                .isEqualTo("The uploaded file is not a valid mp3 file.");
    }

    @Test
    void handleUpload_ofExactlySixMebibytes_isAccepted() {
        enterWithSoundId(null);

        view.handleUpload(uploadEventWith(mp3Bytes(Mp3Format.MAX_BYTES)));

        assertThat(find(Notification.class).all()).isEmpty();
    }

    @Test
    void handleUpload_ofSixMebibytesPlusOne_isRefused() {
        enterWithSoundId(null);

        view.handleUpload(uploadEventWith(mp3Bytes(Mp3Format.MAX_BYTES + 1)));

        assertThat(test(find(Notification.class).single()).getText())
                .isEqualTo("Sounds must be mp3 files of at most 6MB");
    }

    @Test
    void save_staysDisabledWithOnlyAName_orOnlyAFile() {
        enterWithSoundId(null);
        test(nameField()).setValue("tavern");
        assertThat(saveButton().isEnabled()).isFalse();

        enterWithSoundId(null);
        view.handleUpload(uploadEventWith(mp3Bytes(64)));
        assertThat(saveButton().isEnabled()).isFalse();
    }

    @Test
    void save_isEnabledWithANameAndAFile() {
        enterWithSoundId(null);
        view.handleUpload(uploadEventWith(mp3Bytes(64)));

        test(nameField()).setValue("tavern");

        assertThat(saveButton().isEnabled()).isTrue();
    }

    @Test
    void aNameDifferingFromAnotherSoundOnlyByCaseOrSpaces_isRefused() {
        enterWithSoundId(null);
        view.handleUpload(uploadEventWith(mp3Bytes(64)));

        test(nameField()).setValue("  PIRATE_Bay ");

        assertThat(nameField().isInvalid()).isTrue();
        assertThat(nameField().getErrorMessage()).isEqualTo("A sound with this name already exists");
        assertThat(saveButton().isEnabled()).isFalse();
    }

    @Test
    void savingASoundUnderItsOwnName_isAllowed() {
        enterWithSoundId("snd-1");
        view.handleUpload(uploadEventWith(mp3Bytes(64)));

        assertThat(nameField().isInvalid()).isFalse();
        assertThat(saveButton().isEnabled()).isTrue();
    }

    @Test
    void save_storesTheSoundInTheAdventureAndCallsTheServices() {
        enterWithSoundId(null);
        byte[] bytes = mp3Bytes(64);
        view.handleUpload(uploadEventWith(bytes));
        test(nameField()).setValue("tavern");

        test(saveButton()).click();

        ArgumentCaptor<SoundData> saved = ArgumentCaptor.forClass(SoundData.class);
        verify(soundService).save(saved.capture(), eq(bytes));
        assertThat(saved.getValue().getName()).isEqualTo("tavern");
        assertThat(saved.getValue().getAdventureId()).isEqualTo("adv-1");
        assertThat(adventureData.getSoundData()).containsKey(saved.getValue().getId());
        verify(adventureService).saveAdventureData(adventureData);
    }

    @Test
    void renamingWithoutANewFile_savesWithNullContent() {
        enterWithSoundId("snd-1");

        test(nameField()).setValue("renamed");
        test(saveButton()).click();

        verify(soundService).save(soundData, null);
        assertThat(soundData.getName()).isEqualTo("renamed");
        verify(adventureService).saveAdventureData(adventureData);
    }

    @Test
    void deleteButton_isDisabledForANewSound() {
        enterWithSoundId(null);

        assertThat(find(Button.class, view).withText("Delete").single().isEnabled()).isFalse();
    }

    @Test
    void deletingASoundUsedByAPlayAction_isRefusedAndShowsTheUsage() {
        WorkflowData workflow = new WorkflowData();
        PlayActionData play = new PlayActionData();
        play.setSoundId("snd-1");
        play.setTimes(1);
        CommandData command = new CommandData(new CommandDescriptionData(new Word("tick", Word.Type.VERB), null, null));
        command.addAction(play);
        workflow.getCommands().add(command);
        adventureData.setWorkflowData(workflow);
        enterWithSoundId("snd-1");

        test(find(Button.class, view).withText("Delete").single()).click();

        assertThat(find(ConfirmDialog.class).single()).isNotNull();
        verify(soundService, never()).delete(any());
        assertThat(adventureData.getSoundData()).containsKey("snd-1");
    }

    @Test
    void confirmingDeleteOfAnUnusedSound_removesItFromTheAdventureAndTheStore() {
        enterWithSoundId("snd-1");

        ConfirmDialog dialog = view.buildDeleteConfirmDialog();
        UI.getCurrent().add(dialog);
        dialog.open();
        test(dialog).confirm();

        assertThat(adventureData.getSoundData()).doesNotContainKey("snd-1");
        verify(adventureService).saveAdventureData(adventureData);
        verify(soundService).delete("snd-1");
    }
}
