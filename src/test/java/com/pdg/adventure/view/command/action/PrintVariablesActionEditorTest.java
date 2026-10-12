package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.PrintVariablesActionData;

class PrintVariablesActionEditorTest {

    private final PrintVariablesActionData data = new PrintVariablesActionData();
    private final PrintVariablesActionEditor editor = new PrintVariablesActionEditor(data);

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
