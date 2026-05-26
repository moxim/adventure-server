package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.InventoryActionData;

class InventoryActionEditorTest {

    private InventoryActionData actionData;

    @BeforeEach
    void setUp() {
        actionData = new InventoryActionData();
    }

    @Test
    void validate_shouldAlwaysReturnTrue() {
        // Given
        InventoryActionEditor editor = new InventoryActionEditor(actionData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        // When
        InventoryActionEditor editor = new InventoryActionEditor(actionData);

        // Then
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        InventoryActionEditor editor = new InventoryActionEditor(actionData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionTypeName_shouldReturnInventoryActionName() {
        // Given
        InventoryActionEditor editor = new InventoryActionEditor(actionData);

        // When
        String actionTypeName = editor.getActionTypeName();

        // Then
        assertThat(actionTypeName).isEqualTo(actionData.getActionName());
    }
}
