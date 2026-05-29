package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.EqualsConditionData;

class EqualsConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        EqualsConditionEditor editor = new EqualsConditionEditor(new EqualsConditionData("", ""));
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withBothFieldsPreSet_returnsTrue() {
        EqualsConditionData data = new EqualsConditionData("score", "100");
        EqualsConditionEditor editor = new EqualsConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        EqualsConditionData data = new EqualsConditionData("", "");
        EqualsConditionEditor editor = new EqualsConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        EqualsConditionEditor editor = new EqualsConditionEditor(new EqualsConditionData("", ""));
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withPreSetValues_returnsFormattedString() {
        EqualsConditionData data = new EqualsConditionData("lives", "3");
        EqualsConditionEditor editor = new EqualsConditionEditor(data);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("lives = 3");
    }
}
