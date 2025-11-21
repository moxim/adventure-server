package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;

class ActionEditorFactoryTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Set up location data
        Map<String, LocationData> locations = new HashMap<>();
        LocationData location = new LocationData();
        location.setId("loc-1");
        locations.put("loc-1", location);
        adventureData.setLocationData(locations);

        // Set up messages
        Map<String, MessageData> messages = new HashMap<>();
        MessageData message = new MessageData();
        message.setMessageId("msg-1");
        message.setText("Test message");
        messages.put("msg-1", message);
        adventureData.setMessages(messages);

        // Set up player pocket
        ItemContainerData playerPocket = new ItemContainerData("player-pocket");
        adventureData.setPlayerPocket(playerPocket);
    }

    @Test
    void createEditor_withMovePlayerActionData_shouldReturnMovePlayerActionEditor() {
        // Given
        MovePlayerActionData actionData = new MovePlayerActionData();

        // When
        ActionEditorComponent editor = ActionEditorFactory.createEditor(actionData, adventureData);

        // Then
        assertThat(editor).isNotNull().isInstanceOf(MovePlayerActionEditor.class);
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void createEditor_withMoveItemActionData_shouldReturnMoveItemActionEditor() {
        // Given
        MoveItemActionData actionData = new MoveItemActionData();

        // When
        ActionEditorComponent editor = ActionEditorFactory.createEditor(actionData, adventureData);

        // Then
        assertThat(editor).isNotNull().isInstanceOf(MoveItemActionEditor.class);
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void createEditor_withMessageActionData_shouldReturnMessageActionEditor() {
        // Given
        MessageActionData actionData = new MessageActionData();

        // When
        ActionEditorComponent editor = ActionEditorFactory.createEditor(actionData, adventureData);

        // Then
        assertThat(editor).isNotNull().isInstanceOf(MessageActionEditor.class);
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void createEditor_withUnsupportedActionType_shouldThrowUnsupportedOperationException() {
        // Given
        ActionData unsupportedAction = new UnsupportedActionData();

        // When & Then
        assertThatThrownBy(() -> ActionEditorFactory.createEditor(unsupportedAction, adventureData))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("No editor available for action type")
            .hasMessageContaining("UnsupportedActionData");
    }

    @Test
    void createEditor_shouldInitializeEditorUI() {
        // Given
        MovePlayerActionData actionData = new MovePlayerActionData();

        // When
        ActionEditorComponent editor = ActionEditorFactory.createEditor(actionData, adventureData);

        // Then - Editor should be initialized (UI built)
        assertThat(editor).isNotNull();
        // The editor should have child components after initialization
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    /**
     * Test helper class representing an unsupported action type
     */
    private static class UnsupportedActionData extends ActionData {
        @Override
        public String getActionName() {
            return "Unsupported Action";
        }
    }
}
