package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;

/**
 * Component for selecting an action type and creating its corresponding editor.
 * Displays a dropdown list of available action types and a "Use" button to create the editor.
 */
public class ActionSelector extends HorizontalLayout {
    private final AdventureData adventureData;
    private final ComboBox<ActionTypeDescriptor> actionTypeSelector;
    private final Button useButton;
    private ActionEditorSelectedListener editorSelectedListener;

    public ActionSelector(AdventureData adventureData) {
        this.adventureData = adventureData;

        // Create the dropdown with all available action types
        actionTypeSelector = new ComboBox<>("Select Action Type");
        actionTypeSelector.setItems(getAvailableActionTypes());
        actionTypeSelector.setItemLabelGenerator(ActionTypeDescriptor::getDisplayName);
        actionTypeSelector.setPlaceholder("Choose an action...");
        actionTypeSelector.setWidthFull();

        // Create the Use button
        useButton = new Button("Use");
        useButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        useButton.setEnabled(false);

        // Enable the button only when an action is selected
        actionTypeSelector.addValueChangeListener(e -> {
            useButton.setEnabled(e.getValue() != null);
        });

        // Handle the Use button click
        useButton.addClickListener(e -> {
            ActionTypeDescriptor selectedType = actionTypeSelector.getValue();
            if (selectedType != null) {
                ActionEditorComponent editor = createEditor(selectedType);
                if (editorSelectedListener != null && editor != null) {
                    editorSelectedListener.onEditorSelected(editor);
                }
            }
        });

        // Layout setup
        setWidthFull();
        setAlignItems(Alignment.END);
        add(actionTypeSelector, useButton);
        expand(actionTypeSelector);
    }

    /**
     * Creates an editor for the selected action type.
     */
    private ActionEditorComponent createEditor(ActionTypeDescriptor descriptor) {
        ActionData actionData = descriptor.createActionData();
        return ActionEditorFactory.createEditor(actionData, adventureData);
    }

    /**
     * Returns a list of all available action types that have editors.
     */
    private List<ActionTypeDescriptor> getAvailableActionTypes() {
        List<ActionTypeDescriptor> types = new ArrayList<>();

        types.add(new ActionTypeDescriptor(
                "Move Player",
                "Move the player to a different location",
                MovePlayerActionData::new
        ));

        types.add(new ActionTypeDescriptor(
                "Move Item",
                "Move an item to a different container",
                MoveItemActionData::new
        ));

        types.add(new ActionTypeDescriptor(
                "Message",
                "Display a message to the player",
                MessageActionData::new
        ));

        // TODO: Add more action types as editors are implemented
        // types.add(new ActionTypeDescriptor("Set Variable", "Set a variable value", SetVariableActionData::new));

        return types;
    }

    /**
     * Sets the listener that will be notified when an editor is selected.
     */
    public void setEditorSelectedListener(ActionEditorSelectedListener listener) {
        this.editorSelectedListener = listener;
    }

    /**
     * Listener interface for when an action editor is selected.
     */
    @FunctionalInterface
    public interface ActionEditorSelectedListener {
        void onEditorSelected(ActionEditorComponent editor);
    }

    /**
     * Descriptor for an action type, containing display information and a factory method.
     */
    private static class ActionTypeDescriptor {
        private final String displayName;
        private final String description;
        private final Supplier<ActionData> actionFactory;

        public ActionTypeDescriptor(String displayName, String description, Supplier<ActionData> actionFactory) {
            this.displayName = displayName;
            this.description = description;
            this.actionFactory = actionFactory;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public ActionData createActionData() {
            return actionFactory.get();
        }
    }
}
