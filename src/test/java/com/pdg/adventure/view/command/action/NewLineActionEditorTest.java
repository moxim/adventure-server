package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.NewLineActionData;

class NewLineActionEditorTest {

    private final NewLineActionData actionData = new NewLineActionData();

    @Test
    void validate_shouldAlwaysReturnTrue() {
        NewLineActionEditor editor = new NewLineActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEmpty();
    }

    @Test
    void initialize_shouldBuildUI() {
        NewLineActionEditor editor = new NewLineActionEditor(actionData);

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
        assertThat(editor.getActionData()).isSameAs(actionData);
    }
}
