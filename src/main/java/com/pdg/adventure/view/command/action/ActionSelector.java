package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.pdg.adventure.model.action.*;

/**
 * Component for selecting an action type and creating its corresponding editor.
 * Displays a dropdown list of available action types and a "Use" button to create the editor.
 */
public class ActionSelector extends HorizontalLayout {
    private final ComboBox<ActionTypeDescriptor> typeSelector;
    @Setter
    private transient ActionEditorSelectedListener editorSelectedListener;

    public ActionSelector() {

        // Create the dropdown with all available action types
        typeSelector = new ComboBox<>("Select Action Type");
        typeSelector.setItems(getAvailableActionTypes());
        typeSelector.setItemLabelGenerator(ActionTypeDescriptor::displayName);
        typeSelector.setPlaceholder("Choose an action...");
        typeSelector.setWidthFull();
        typeSelector.getStyle().set("--vaadin-combo-box-overlay-width", "28em");

         Button addButton = new Button("Add");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setEnabled(false);

        // Enable the button only when an action is selected
        typeSelector.addValueChangeListener(e -> addButton.setEnabled(e.getValue() != null));

        // Handle the Use button click
        addButton.addClickListener(_ -> {
            ActionTypeDescriptor selected = typeSelector.getValue();
            if (selected != null && editorSelectedListener != null) {
                    editorSelectedListener.onEditorSelected(selected.createData());
                    typeSelector.clear();
                    addButton.setEnabled(false);
            }
        });

        // Layout setup
        setWidthFull();
        setAlignItems(Alignment.END);
        add(typeSelector, addButton);
        expand(typeSelector);
    }

    private List<ActionTypeDescriptor> getAvailableActionTypes() {
        return Stream.of(
        new ActionTypeDescriptor("Move Player", "Move the player to a different location", MovePlayerActionData::new),
        new ActionTypeDescriptor("Move Item", "Move an item to a different container", MoveItemActionData::new),
        new ActionTypeDescriptor("Message", "Display a message to the player", MessageActionData::new),
        new ActionTypeDescriptor("Destroy", "Remove an item from the game permanently", DestroyActionData::new),
        new ActionTypeDescriptor("Remove (Un-wear)", "Remove a wearable item from the player", RemoveActionData::new),
        new ActionTypeDescriptor("Increment Variable", "Increment a named variable by an amount", IncrementVariableActionData::new),
        new ActionTypeDescriptor("Decrement Variable", "Decrement a named variable by an amount", DecrementVariableActionData::new),
        new ActionTypeDescriptor("Describe", "Show the description of an item or location", DescribeActionData::new),
        new ActionTypeDescriptor("Create Item", "Place an item into a container or location", CreateActionData::new),
        new ActionTypeDescriptor("Inventory", "Show the player's inventory", InventoryActionData::new),
        new ActionTypeDescriptor("Take", "Player picks up an item", TakeActionData::new),
        new ActionTypeDescriptor("Drop", "Player drops an item", DropActionData::new),
        new ActionTypeDescriptor("Wear", "Player wears a wearable item", WearActionData::new),
        new ActionTypeDescriptor("Light", "Set an item's light level (lumen)", LightActionData::new),
        new ActionTypeDescriptor("Set Variable", "Set a named variable to a specific value", () -> new SetVariableActionData(null, null)),
        new ActionTypeDescriptor("Quit", "Terminate the game", QuitActionData::new),
        new ActionTypeDescriptor("Break", "Stop processing this command chain; other chains are unaffected", BreakActionData::new)
        ).sorted(Comparator.comparing(ActionTypeDescriptor::displayName, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @FunctionalInterface
    public interface ActionEditorSelectedListener {
        void onEditorSelected(ActionData data);
    }

    private record ActionTypeDescriptor(String displayName, String description, Supplier<ActionData> factory) {
        public ActionData createData() {
            return factory.get();
        }
    }
}
