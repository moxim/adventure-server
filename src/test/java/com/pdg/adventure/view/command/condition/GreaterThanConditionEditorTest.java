package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.GreaterThanConditionData;

class GreaterThanConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(new GreaterThanConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withInvalidNumberValue_returnsFalse() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName("score");
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(data);
        editor.initialize();
        // variableName set but value not set
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPreSetValues_returnsTrue() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName("score");
        data.setValue(50);
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(new GreaterThanConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
