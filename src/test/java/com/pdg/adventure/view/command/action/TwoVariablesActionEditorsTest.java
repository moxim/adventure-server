package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.AddVariableActionData;
import com.pdg.adventure.model.action.SubtractVariableActionData;

class TwoVariablesActionEditorsTest {

    @Test
    void add_validate_withNothingSelected_shouldReturnFalse() {
        AddVariableActionEditor editor = new AddVariableActionEditor(new AddVariableActionData());
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void add_validate_withOnlyFirstSelected_shouldReturnFalse() {
        AddVariableActionEditor editor = new AddVariableActionEditor(new AddVariableActionData("bonus", null));
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void add_validate_withBothSelected_shouldReturnTrue() {
        AddVariableActionEditor editor = new AddVariableActionEditor(new AddVariableActionData("bonus", "score"));
        editor.initialize();

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("score += bonus");
    }

    @Test
    void subtract_validate_withBothSelected_shouldReturnTrue() {
        SubtractVariableActionEditor editor =
                new SubtractVariableActionEditor(new SubtractVariableActionData("cost", "score"));
        editor.initialize();

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("score -= cost");
    }

    @Test
    void subtract_summary_withNothingSelected_isNone() {
        SubtractVariableActionEditor editor = new SubtractVariableActionEditor(new SubtractVariableActionData());
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }
}
