package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.PrintVariableActionData;

class PrintVariableActionEditorTest {

    @Test
    void validate_withNoName_returnsFalse() {
        PrintVariableActionEditor editor = new PrintVariableActionEditor(new PrintVariableActionData());
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withAName_returnsTrue() {
        PrintVariableActionData data = new PrintVariableActionData();
        data.setName("VISITED");
        PrintVariableActionEditor editor = new PrintVariableActionEditor(data);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void initialize_buildsTheUI() {
        PrintVariableActionEditor editor = new PrintVariableActionEditor(new PrintVariableActionData());

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionSummary_withAName_isTheName() {
        PrintVariableActionData data = new PrintVariableActionData();
        data.setName("score");
        PrintVariableActionEditor editor = new PrintVariableActionEditor(data);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("score");
    }

    @Test
    void getActionSummary_withoutAName_saysNone() {
        PrintVariableActionEditor editor = new PrintVariableActionEditor(new PrintVariableActionData());
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }
}
