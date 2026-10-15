package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.DiagnoseVariableActionData;

class DiagnoseVariableActionEditorTest {

    @Test
    void validate_withNoName_returnsFalse() {
        DiagnoseVariableActionEditor editor = new DiagnoseVariableActionEditor(new DiagnoseVariableActionData());
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withAName_returnsTrue() {
        DiagnoseVariableActionData data = new DiagnoseVariableActionData();
        data.setName("VISITED");
        DiagnoseVariableActionEditor editor = new DiagnoseVariableActionEditor(data);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void initialize_buildsTheUI() {
        DiagnoseVariableActionEditor editor = new DiagnoseVariableActionEditor(new DiagnoseVariableActionData());

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionSummary_withAName_isTheName() {
        DiagnoseVariableActionData data = new DiagnoseVariableActionData();
        data.setName("score");
        DiagnoseVariableActionEditor editor = new DiagnoseVariableActionEditor(data);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("score");
    }

    @Test
    void getActionSummary_withoutAName_saysNone() {
        DiagnoseVariableActionEditor editor = new DiagnoseVariableActionEditor(new DiagnoseVariableActionData());
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }
}
