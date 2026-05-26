package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.QuitActionData;

class QuitActionEditorTest {

    @Test
    void constructor_shouldSetActionData() {
        QuitActionData actionData = new QuitActionData();

        QuitActionEditor editor = new QuitActionEditor(actionData);

        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void validate_shouldAlwaysReturnTrue() {
        QuitActionData actionData = new QuitActionData();
        QuitActionEditor editor = new QuitActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void initialize_shouldBuildUIChildren() {
        QuitActionData actionData = new QuitActionData();
        QuitActionEditor editor = new QuitActionEditor(actionData);

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
