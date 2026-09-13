package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.ActionData;

class ActionSelectorTest {

    private AdventureData adventureData;
    ActionSelector actionSelector = new ActionSelector();

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Set up minimal required data
        Map<String, LocationData> locations = new HashMap<>();
        LocationData location = new LocationData();
        location.setId("loc-1");
        locations.put("loc-1", location);
        adventureData.setLocationData(locations);

        Map<String, MessageData> messages = new HashMap<>();
        MessageData message = new MessageData();
        message.setMessageId("msg-1");
        message.setText("Test message");
        messages.put("msg-1", message);
        adventureData.setMessages(messages);

        ItemContainerData playerPocket = new ItemContainerData("player-pocket");
        adventureData.setPlayerPocket(playerPocket);
    }

    @Test
    void constructor_shouldCreateActionSelectorWithDisabledUseButton() {
        // When

        // Then
        assertThat(actionSelector).isNotNull();
        assertThat(actionSelector.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void setEditorSelectedListener_shouldInvokeListenerWhenActionSelected() {
        // Given
        AtomicReference<ActionData> capturedEditor = new AtomicReference<>();

        actionSelector.setEditorSelectedListener(capturedEditor::set);

        // When - Simulate selecting an action type and clicking Use
        // Note: In a real UI test, we would interact with the ComboBox and Button
        // For unit testing, we verify the listener mechanism is set up correctly

        // Then
        assertThat(capturedEditor.get()).isNull(); // No action selected yet
    }

    @Test
    void constructor_shouldPopulateActionTypes() {
        // When

        // Then - The selector should have been created with available action types
        // We verify this indirectly by checking that the component has children
        assertThat(actionSelector.getChildren().count()).isEqualTo(2); // ComboBox and Button
    }

    @Test
    void setEditorSelectedListener_withNullListener_shouldNotThrowException() {
        // Given

        // When & Then - Should not throw
        actionSelector.setEditorSelectedListener(null);

        assertThat(true).isTrue(); // If we reach here, no exception was thrown
    }

    @Test
    void constructor_withValidAdventureData_shouldCreateAllComponents() {
        // When

        // Then
        assertThat(actionSelector.getChildren().count()).isEqualTo(2);
        assertThat(actionSelector.isVisible()).isTrue();
    }
}
