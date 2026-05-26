package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.SetVariableActionData;

class SetVariableActionEditorTest {

    @Test
    void validate_withNoNameAndNoValue_shouldReturnFalse() {
        SetVariableActionData actionData = new SetVariableActionData(null, null);
        SetVariableActionEditor editor = new SetVariableActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withNameButNoValue_shouldReturnFalse() {
        SetVariableActionData actionData = new SetVariableActionData("score", null);
        SetVariableActionEditor editor = new SetVariableActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withNameAndValue_shouldReturnTrue() {
        SetVariableActionData actionData = new SetVariableActionData("score", "100");
        SetVariableActionEditor editor = new SetVariableActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        SetVariableActionData actionData = new SetVariableActionData(null, null);
        SetVariableActionEditor editor = new SetVariableActionEditor(actionData);

        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        SetVariableActionData actionData = new SetVariableActionData(null, null);
        SetVariableActionEditor editor = new SetVariableActionEditor(actionData);

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
