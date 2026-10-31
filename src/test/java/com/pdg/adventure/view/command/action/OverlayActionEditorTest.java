package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.action.OverlayActionData;

class OverlayActionEditorTest {

    private final AdventureData adventureData = new AdventureData();
    private final PictureData door = picture("door-open", "Open door");

    OverlayActionEditorTest() {
        adventureData.setPictureData(new HashMap<>());
        adventureData.getPictureData().put(door.getId(), door);
    }

    private static PictureData picture(String id, String name) {
        PictureData picture = new PictureData();
        picture.setId(id);
        picture.setName(name);
        return picture;
    }

    private OverlayActionEditor editorFor(OverlayActionData data) {
        OverlayActionEditor editor = new OverlayActionEditor(data, adventureData);
        editor.initialize();
        return editor;
    }

    private static List<IntegerField> cellFields(OverlayActionEditor editor) {
        return editor.getChildren().filter(IntegerField.class::isInstance).map(IntegerField.class::cast).toList();
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<PictureData> pictures(OverlayActionEditor editor) {
        return (ComboBox<PictureData>) editor.getChildren().filter(ComboBox.class::isInstance).findFirst().orElseThrow();
    }

    @Test
    void validate_withNothingChosen_returnsFalse() {
        OverlayActionEditor editor = editorFor(new OverlayActionData());

        assertThat(editor.validate()).isFalse();
        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void savedValues_arePreselected_andSummarised() {
        OverlayActionData data = new OverlayActionData();
        data.setX(3);
        data.setY(4);
        data.setPictureId("door-open");
        OverlayActionEditor editor = editorFor(data);

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("3, 4 Open door");
    }

    @Test
    void editingTheFields_writesThemToTheData() {
        OverlayActionData data = new OverlayActionData();
        OverlayActionEditor editor = editorFor(data);

        List<IntegerField> fields = cellFields(editor);
        fields.get(0).setValue(7);
        fields.get(1).setValue(2);
        pictures(editor).setValue(door);

        assertThat(data.getX()).isEqualTo(7);
        assertThat(data.getY()).isEqualTo(2);
        assertThat(data.getPictureId()).isEqualTo("door-open");
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void validate_withACellOutsideTheGrid_returnsFalse() {
        OverlayActionData data = new OverlayActionData();
        data.setX(10);
        data.setY(0);
        data.setPictureId("door-open");

        assertThat(editorFor(data).validate()).isFalse();
    }
}
