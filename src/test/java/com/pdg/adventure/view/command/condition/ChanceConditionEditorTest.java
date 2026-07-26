package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.ChanceConditionData;

class ChanceConditionEditorTest {

    @Test
    void validate_withEmptyValue_returnsFalse() {
        ChanceConditionEditor editor = new ChanceConditionEditor(new ChanceConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPreSetValue_returnsTrue() {
        ChanceConditionData data = new ChanceConditionData();
        data.setValue(75);
        ChanceConditionEditor editor = new ChanceConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void validate_withBoundaryValues_returnsTrue() {
        ChanceConditionData one = new ChanceConditionData();
        one.setValue(1);
        ChanceConditionEditor zeroEditor = new ChanceConditionEditor(one);
        zeroEditor.initialize();
        assertThat(zeroEditor.validate()).isTrue();

        ChanceConditionData hundred = new ChanceConditionData();
        hundred.setValue(100);
        ChanceConditionEditor hundredEditor = new ChanceConditionEditor(hundred);
        hundredEditor.initialize();
        assertThat(hundredEditor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        ChanceConditionData data = new ChanceConditionData();
        ChanceConditionEditor editor = new ChanceConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        ChanceConditionEditor editor = new ChanceConditionEditor(new ChanceConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withValue_reportsPercentage() {
        ChanceConditionData data = new ChanceConditionData();
        data.setValue(30);
        ChanceConditionEditor editor = new ChanceConditionEditor(data);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("30% chance");
    }

    @Test
    void getConditionSummary_withoutValue_reportsNone() {
        ChanceConditionEditor editor = new ChanceConditionEditor(new ChanceConditionData());
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }
}
