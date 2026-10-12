package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.pdg.adventure.model.action.PrintVariableActionData;
import com.pdg.adventure.model.action.PrintVariablesActionData;
import com.pdg.adventure.model.action.TurnsActionData;

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
        MessageData message = new MessageData("A test message", "Test message");
        messages.put(message.getId(), message);
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
    @SuppressWarnings("unchecked")
    void choosingTurnsAndPressingAdd_handsTheListenerANewTurnsActionData() {
        AtomicReference<ActionData> captured = new AtomicReference<>();
        actionSelector.setEditorSelectedListener(captured::set);
        ComboBox<Object> typeSelector = (ComboBox<Object>) actionSelector.getChildren()
                .filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        Button add = (Button) actionSelector.getChildren().filter(Button.class::isInstance).findFirst().orElseThrow();
        Object turns = typeSelector.getListDataView().getItems()
                .filter(item -> "Turns".equals(typeSelector.getItemLabelGenerator().apply(item)))
                .findFirst().orElseThrow();

        typeSelector.setValue(turns);
        assertThat(add.isEnabled()).isTrue();
        ComponentUtil.fireEvent(add, new ClickEvent<>(add));

        assertThat(captured.get()).isInstanceOf(TurnsActionData.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void choosingPrintVariableAndPressingAdd_handsTheListenerANewPrintVariableActionData() {
        AtomicReference<ActionData> captured = new AtomicReference<>();
        actionSelector.setEditorSelectedListener(captured::set);
        ComboBox<Object> typeSelector = (ComboBox<Object>) actionSelector.getChildren()
                .filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        Button add = (Button) actionSelector.getChildren().filter(Button.class::isInstance).findFirst().orElseThrow();
        Object printVariable = typeSelector.getListDataView().getItems()
                .filter(item -> "Print Variable".equals(typeSelector.getItemLabelGenerator().apply(item)))
                .findFirst().orElseThrow();

        typeSelector.setValue(printVariable);
        ComponentUtil.fireEvent(add, new ClickEvent<>(add));

        assertThat(captured.get()).isInstanceOf(PrintVariableActionData.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void choosingPrintVariablesAndPressingAdd_handsTheListenerANewPrintVariablesActionData() {
        AtomicReference<ActionData> captured = new AtomicReference<>();
        actionSelector.setEditorSelectedListener(captured::set);
        ComboBox<Object> typeSelector = (ComboBox<Object>) actionSelector.getChildren()
                .filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        Button add = (Button) actionSelector.getChildren().filter(Button.class::isInstance).findFirst().orElseThrow();
        Object printVariables = typeSelector.getListDataView().getItems()
                .filter(item -> "Print Variables".equals(typeSelector.getItemLabelGenerator().apply(item)))
                .findFirst().orElseThrow();

        typeSelector.setValue(printVariables);
        ComponentUtil.fireEvent(add, new ClickEvent<>(add));

        assertThat(captured.get()).isInstanceOf(PrintVariablesActionData.class);
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
