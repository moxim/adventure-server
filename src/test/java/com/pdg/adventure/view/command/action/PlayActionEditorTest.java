package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.model.action.PlayActionData;

class PlayActionEditorTest {

    private AdventureData adventureData;
    private SoundData tavern;
    private SoundData pirateBay;
    private PlayActionData playActionData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        tavern = sound("sound-1", "tavern");
        pirateBay = sound("sound-2", "Pirate_Bay");
        adventureData.getSoundData().put(tavern.getId(), tavern);
        adventureData.getSoundData().put(pirateBay.getId(), pirateBay);
        playActionData = new PlayActionData();
    }

    private static SoundData sound(String id, String name) {
        SoundData sound = new SoundData();
        sound.setId(id);
        sound.setName(name);
        return sound;
    }

    private PlayActionEditor editor() {
        PlayActionEditor editor = new PlayActionEditor(playActionData, adventureData);
        editor.initialize();
        return editor;
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<SoundData> soundBox(PlayActionEditor anEditor) {
        return (ComboBox<SoundData>) anEditor.firstSelectBox().orElseThrow();
    }

    private static IntegerField timesField(PlayActionEditor anEditor) {
        return anEditor.getChildren().filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                       .findFirst().orElseThrow();
    }

    @Test
    void soundsAreOfferedSortedByNameIgnoringCase() {
        ComboBox<SoundData> box = soundBox(editor());

        assertThat(box.getListDataView().getItems()).extracting(SoundData::getName)
                                                   .containsExactly("Pirate_Bay", "tavern");
    }

    @Test
    void preselectsTheStoredSoundAndTimes() {
        playActionData.setSoundId("sound-2");
        playActionData.setTimes(2);

        PlayActionEditor editor = editor();

        assertThat(soundBox(editor).getValue()).isSameAs(pirateBay);
        assertThat(timesField(editor).getValue()).isEqualTo(2);
        assertThat(editor.getActionSummary()).isEqualTo("Pirate_Bay 2");
    }

    @Test
    void summary_withoutASound_isNone() {
        assertThat(editor().getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void validate_isFalseWithoutASound() {
        playActionData.setTimes(1);

        assertThat(editor().validate()).isFalse();
    }

    @Test
    void validate_isFalseWithoutTimes() {
        playActionData.setSoundId("sound-1");

        assertThat(editor().validate()).isFalse();
    }

    @Test
    void validate_isFalseForTimesOutsideZeroToNinetyNine() {
        playActionData.setSoundId("sound-1");
        PlayActionEditor editor = editor();

        timesField(editor).setValue(-1);
        assertThat(editor.validate()).isFalse();

        timesField(editor).setValue(100);
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_acceptsZeroAndNinetyNine() {
        playActionData.setSoundId("sound-1");
        PlayActionEditor editor = editor();

        timesField(editor).setValue(0);
        assertThat(editor.validate()).isTrue();

        timesField(editor).setValue(99);
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void changingTheSelection_updatesTheActionData() {
        PlayActionEditor editor = editor();

        soundBox(editor).setValue(tavern);
        timesField(editor).setValue(3);

        assertThat(playActionData.getSoundId()).isEqualTo("sound-1");
        assertThat(playActionData.getTimes()).isEqualTo(3);
        assertThat(editor.getActionSummary()).isEqualTo("tavern 3");
    }

    @Test
    void clearingTheSound_clearsTheActionDataSoundId() {
        playActionData.setSoundId("sound-1");
        PlayActionEditor editor = editor();

        soundBox(editor).clear();

        assertThat(playActionData.getSoundId()).isNull();
        assertThat((Component) soundBox(editor)).isNotNull();
    }
}
