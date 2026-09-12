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

    @Test
    void getConditionSummary_withBothFieldsEmpty_doesNotThrow() {
        // Reproduces the NPE hit when a brand-new row is added to the Arrivals condition table:
        // ConditionRow's constructor calls getConditionSummary() immediately, before either field
        // has been touched, so IntegerField.getValue() is still null.
        LowerThanConditionEditor editor = new LowerThanConditionEditor(new LowerThanConditionData());
        editor.initialize();

        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }

    @Test
    void getConditionSummary_withNameButNoValue_omitsTheValue() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setVariableName("lives");
        LowerThanConditionEditor editor = new LowerThanConditionEditor(data);
        editor.initialize();

        assertThat(editor.getConditionSummary()).isEqualTo("lives < ");
    }

    @Test
    void getConditionSummary_withPreSetValues_includesBoth() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);
        LowerThanConditionEditor editor = new LowerThanConditionEditor(data);
        editor.initialize();

        assertThat(editor.getConditionSummary()).isEqualTo("lives < 3");
    }
}
