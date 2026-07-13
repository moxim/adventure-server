package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.IncrementVariableActionData;

class IncrementVariableActionEditorTest {

    @Test
    void validate_withNoNameAndNoValue_shouldReturnFalse() {
        // Given
        IncrementVariableActionData actionData = new IncrementVariableActionData();
        IncrementVariableActionEditor editor = new IncrementVariableActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withNameButNoValue_shouldReturnFalse() {
        // Given
        IncrementVariableActionData actionData = new IncrementVariableActionData();
        actionData.setName("score");
        IncrementVariableActionEditor editor = new IncrementVariableActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withNameAndValue_shouldReturnTrue() {
        // Given
        IncrementVariableActionData actionData = new IncrementVariableActionData();
        actionData.setName("score");
        actionData.setValue("10");
        IncrementVariableActionEditor editor = new IncrementVariableActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        // Given
        IncrementVariableActionData actionData = new IncrementVariableActionData();

        // When
        IncrementVariableActionEditor editor = new IncrementVariableActionEditor(actionData);

        // Then
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        IncrementVariableActionData actionData = new IncrementVariableActionData();
        IncrementVariableActionEditor editor = new IncrementVariableActionEditor(actionData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionSummary_withNameAndValue_returnsIncrement() {
        IncrementVariableActionData actionData = new IncrementVariableActionData();
        actionData.setName("score");
        actionData.setValue("5");
        IncrementVariableActionEditor editor = new IncrementVariableActionEditor(actionData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("score += 5");
    }
}
