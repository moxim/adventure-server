package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.model.action.WearActionData;

/**
 * Component for selecting an action type and creating its corresponding editor.
 * Displays a dropdown list of available action types and a "Use" button to create the editor.
 */
public class ActionSelector extends HorizontalLayout {
    private final AdventureData adventureData;
    private final ComboBox<ActionTypeDescriptor> actionTypeSelector;
    private final Button useButton;
    @Setter
    private transient ActionEditorSelectedListener editorSelectedListener;

    public ActionSelector(AdventureData adventureData) {
        this.adventureData = adventureData;

        // Create the dropdown with all available action types
        actionTypeSelector = new ComboBox<>("Select Action Type");
        actionTypeSelector.setItems(getAvailableActionTypes());
        actionTypeSelector.setItemLabelGenerator(ActionTypeDescriptor::displayName);
        actionTypeSelector.setPlaceholder("Choose an action...");
        actionTypeSelector.setWidthFull();

        // Create the Use button
        useButton = new Button("Use");
        useButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        useButton.setEnabled(false);

        // Enable the button only when an action is selected
        actionTypeSelector.addValueChangeListener(e -> useButton.setEnabled(e.getValue() != null));

        // Handle the Use button click
        useButton.addClickListener(_ -> {
            ActionTypeDescriptor selectedType = actionTypeSelector.getValue();
            if (selectedType != null) {
                ActionEditorComponent editor = createEditor(selectedType);
                if (editorSelectedListener != null) {
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
     * Returns a list of all available action types that have editors.
     */
    private List<ActionTypeDescriptor> getAvailableActionTypes() {
        List<ActionTypeDescriptor> types = new ArrayList<>();

        types.add(new ActionTypeDescriptor("Move Player", "Move the player to a different location",
                                           MovePlayerActionData::new));

        types.add(new ActionTypeDescriptor("Move Item", "Move an item to a different container",
                                           MoveItemActionData::new));

        types.add(new ActionTypeDescriptor("Message", "Display a message to the player", MessageActionData::new));

        types.add(new ActionTypeDescriptor("Destroy", "Remove an item from the game permanently",
                                           DestroyActionData::new));
        types.add(new ActionTypeDescriptor("Remove (Un-wear)", "Remove a wearable item from the player",
                                           RemoveActionData::new));
        types.add(new ActionTypeDescriptor("Increment Variable", "Increment a named variable by an amount",
                                           IncrementVariableActionData::new));
        types.add(new ActionTypeDescriptor("Decrement Variable", "Decrement a named variable by an amount",
                                           DecrementVariableActionData::new));
        types.add(new ActionTypeDescriptor("Describe", "Show the description of an item or location",
                                           DescribeActionData::new));
        types.add(new ActionTypeDescriptor("Create Item", "Place an item into a container or location",
                                           CreateActionData::new));
        types.add(new ActionTypeDescriptor("Inventory", "Show the player's inventory", InventoryActionData::new));
        types.add(new ActionTypeDescriptor("Take", "Player picks up an item", TakeActionData::new));
        types.add(new ActionTypeDescriptor("Drop", "Player drops an item", DropActionData::new));
        types.add(new ActionTypeDescriptor("Wear", "Player wears a wearable item", WearActionData::new));
        types.add(new ActionTypeDescriptor("Set Variable", "Set a named variable to a specific value",
                                           () -> new SetVariableActionData(null, null)));
        types.add(new ActionTypeDescriptor("Quit", "Terminate the game", QuitActionData::new));

        return types;
    }

    /**
     * Creates an editor for the selected action type.
     */
    private ActionEditorComponent createEditor(ActionTypeDescriptor descriptor) {
        ActionData actionData = descriptor.createActionData();
        return ActionEditorFactory.createEditor(actionData, adventureData);
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
    private record ActionTypeDescriptor(String displayName, String description, Supplier<ActionData> actionFactory) {

        public ActionData createActionData() {
            return actionFactory.get();
        }
    }
}
