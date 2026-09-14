package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.LessThanConditionData;

class LessThanConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        LessThanConditionEditor editor = new LessThanConditionEditor(new LessThanConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPreSetValues_returnsTrue() {
        LessThanConditionData data = new LessThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);
        LessThanConditionEditor editor = new LessThanConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        LessThanConditionData data = new LessThanConditionData();
        LessThanConditionEditor editor = new LessThanConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        LessThanConditionEditor editor = new LessThanConditionEditor(new LessThanConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withBothFieldsEmpty_doesNotThrow() {
        // Reproduces the NPE hit when a brand-new row is added to the Arrivals condition table:
        // ConditionRow's constructor calls getConditionSummary() immediately, before either field
        // has been touched, so IntegerField.getValue() is still null.
        LessThanConditionEditor editor = new LessThanConditionEditor(new LessThanConditionData());
        editor.initialize();

        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }

    @Test
    void getConditionSummary_withNameButNoValue_omitsTheValue() {
        LessThanConditionData data = new LessThanConditionData();
        data.setVariableName("lives");
        LessThanConditionEditor editor = new LessThanConditionEditor(data);
        editor.initialize();

        assertThat(editor.getConditionSummary()).isEqualTo("lives < ");
    }

    @Test
    void getConditionSummary_withPreSetValues_includesBoth() {
        LessThanConditionData data = new LessThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);
        LessThanConditionEditor editor = new LessThanConditionEditor(data);
        editor.initialize();

        assertThat(editor.getConditionSummary()).isEqualTo("lives < 3");
    }
}
