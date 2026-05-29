package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.LowerThanConditionData;

class LowerThanConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        LowerThanConditionEditor editor = new LowerThanConditionEditor(new LowerThanConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPreSetValues_returnsTrue() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);
        LowerThanConditionEditor editor = new LowerThanConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        LowerThanConditionData data = new LowerThanConditionData();
        LowerThanConditionEditor editor = new LowerThanConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        LowerThanConditionEditor editor = new LowerThanConditionEditor(new LowerThanConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
