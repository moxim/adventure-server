package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.DecrementVariableActionData;

class DecrementVariableActionEditorTest {

    private DecrementVariableActionData actionData;

    @BeforeEach
    void setUp() {
        actionData = new DecrementVariableActionData();
    }

    @Test
    void validate_withNoNameAndNoValue_shouldReturnFalse() {
        // Given
        DecrementVariableActionEditor editor = new DecrementVariableActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withNameButNoValue_shouldReturnFalse() {
        // Given
        actionData.setName("score");
        DecrementVariableActionEditor editor = new DecrementVariableActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withNameAndValue_shouldReturnTrue() {
        // Given
        actionData.setName("score");
        actionData.setValue("10");
        DecrementVariableActionEditor editor = new DecrementVariableActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        // When
        DecrementVariableActionEditor editor = new DecrementVariableActionEditor(actionData);

        // Then
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        DecrementVariableActionEditor editor = new DecrementVariableActionEditor(actionData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
