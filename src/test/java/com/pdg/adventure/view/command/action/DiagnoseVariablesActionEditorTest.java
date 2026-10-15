package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.DiagnoseVariablesActionData;

class DiagnoseVariablesActionEditorTest {

    private final DiagnoseVariablesActionData data = new DiagnoseVariablesActionData();
    private final DiagnoseVariablesActionEditor editor = new DiagnoseVariablesActionEditor(data);

    @Test
    void initialize_buildsTheUI() {
        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
        assertThat(editor.getActionData()).isSameAs(data);
    }

    @Test
    void validate_isAlwaysTrue_sinceThereIsNothingToConfigure() {
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void getActionSummary_isEmpty() {
        editor.initialize();

        assertThat(editor.getActionSummary()).isEmpty();
    }
}
