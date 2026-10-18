package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.ClickedConditionData;

class ClickedConditionEditorTest {

    private static ClickedConditionEditor editorFor(Integer x, Integer y) {
        ClickedConditionData data = new ClickedConditionData();
        data.setX(x);
        data.setY(y);
        ClickedConditionEditor editor = new ClickedConditionEditor(data);
        editor.initialize();
        return editor;
    }

    private static List<IntegerField> fields(ClickedConditionEditor editor) {
        return editor.getChildren().filter(IntegerField.class::isInstance).map(IntegerField.class::cast).toList();
    }

    @Test
    void initialize_buildsTwoRequiredFieldsWithTheGridLabels() {
        List<IntegerField> fields = fields(editorFor(null, null));

        assertThat(fields).extracting(IntegerField::getLabel).containsExactly("X (column, 0–9)", "Y (row, 0–9)");
        assertThat(fields).allSatisfy(field -> assertThat(field.isRequiredIndicatorVisible()).isTrue());
    }

    @Test
    void initialize_loadsTheValuesFromTheData() {
        List<IntegerField> fields = fields(editorFor(3, 4));

        assertThat(fields.get(0).getValue()).isEqualTo(3);
        assertThat(fields.get(1).getValue()).isEqualTo(4);
    }

    @Test
    void editingTheFields_writesBackToTheData() {
        ClickedConditionEditor editor = editorFor(null, null);
        List<IntegerField> fields = fields(editor);

        fields.get(0).setValue(7);
        fields.get(1).setValue(9);

        assertThat(editor.getConditionData().getX()).isEqualTo(7);
        assertThat(editor.getConditionData().getY()).isEqualTo(9);
    }

    @Test
    void validate_withEmptyFields_returnsFalse() {
        assertThat(editorFor(null, null).validate()).isFalse();
        assertThat(editorFor(3, null).validate()).isFalse();
        assertThat(editorFor(null, 4).validate()).isFalse();
    }

    @Test
    void validate_acceptsTheBoundaries0And9() {
        assertThat(editorFor(0, 0).validate()).isTrue();
        assertThat(editorFor(9, 9).validate()).isTrue();
    }

    @Test
    void validate_rejectsValuesOutsideTheGrid() {
        assertThat(editorFor(-1, 4).validate()).isFalse();
        assertThat(editorFor(3, 10).validate()).isFalse();
    }

    @Test
    void validate_marksOnlyTheInvalidFieldAsInvalid() {
        ClickedConditionEditor editor = editorFor(3, 10);

        editor.validate();

        List<IntegerField> fields = fields(editor);
        assertThat(fields.get(0).isInvalid()).isFalse();
        assertThat(fields.get(1).isInvalid()).isTrue();
    }

    @Test
    void getConditionSummary_isNoneUntilBothAreFilled_thenShowsTheCell() {
        assertThat(editorFor(null, null).getConditionSummary()).isEqualTo("(none)");
        assertThat(editorFor(3, null).getConditionSummary()).isEqualTo("(none)");
        assertThat(editorFor(3, 4).getConditionSummary()).isEqualTo("clicked 3, 4");
    }
}
