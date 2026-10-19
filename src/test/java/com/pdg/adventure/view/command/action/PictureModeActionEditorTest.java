package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.model.action.PictureModeActionData;

class PictureModeActionEditorTest {

    private PictureModeActionEditor editorFor(PictureModeActionData data) {
        PictureModeActionEditor editor = new PictureModeActionEditor(data, new AdventureData());
        editor.initialize();
        return editor;
    }

    @Test
    void validate_withNoModeChosen_returnsFalse() {
        PictureModeActionEditor editor = editorFor(new PictureModeActionData());

        assertThat(editor.validate()).isFalse();
        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void aSavedMode_isPreselected_andSummarised() {
        PictureModeActionData data = new PictureModeActionData();
        data.setMode(PictureMode.OFF);
        PictureModeActionEditor editor = editorFor(data);

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("OFF");
    }

    @Test
    void choosingAMode_writesItToTheData() {
        PictureModeActionData data = new PictureModeActionData();
        PictureModeActionEditor editor = editorFor(data);

        @SuppressWarnings("unchecked")
        ComboBox<PictureMode> modes = (ComboBox<PictureMode>) editor.getChildren()
                .filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        modes.setValue(PictureMode.ON);

        assertThat(data.getMode()).isEqualTo(PictureMode.ON);
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void theSelectBox_isTheFirstOneForFocus() {
        assertThat(editorFor(new PictureModeActionData()).firstSelectBox()).isPresent();
    }
}
