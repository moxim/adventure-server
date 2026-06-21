package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.SameConditionData;

class SameConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        SameConditionEditor editor = new SameConditionEditor(new SameConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withBothVariablesPreSet_returnsTrue() {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne("score");
        data.setVariableNameTwo("highScore");
        SameConditionEditor editor = new SameConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        SameConditionData data = new SameConditionData();
        SameConditionEditor editor = new SameConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        SameConditionEditor editor = new SameConditionEditor(new SameConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withBothSet_returnsFormattedString() {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne("a");
        data.setVariableNameTwo("b");
        SameConditionEditor editor = new SameConditionEditor(data);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("a = b");
    }
}
