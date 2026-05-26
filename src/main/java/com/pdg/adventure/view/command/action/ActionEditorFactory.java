package com.pdg.adventure.view.command.action;

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
import com.pdg.adventure.model.action.WearActionData;

/**
 * Factory for creating action editor components.
 * This factory uses the action data type to determine which specific editor to create.
 */
public class ActionEditorFactory {

    /**
     * Create an editor component for the given action data.
     * The editor is fully initialized and ready to use.
     *
     * @param actionData    the action data to edit
     * @param adventureData the adventure data (needed for context like locations, items, etc.)
     * @return the appropriate editor component, fully initialized
     * @throws UnsupportedOperationException if no editor is available for the action type
     */
    public static ActionEditorComponent createEditor(ActionData actionData, AdventureData adventureData) {

        ActionEditorComponent editor = switch (actionData) {
            case MovePlayerActionData movePlayerActionData ->
                    new MovePlayerActionEditor(movePlayerActionData, adventureData);
            case MoveItemActionData moveItemActionData -> new MoveItemActionEditor(moveItemActionData, adventureData);
            case MessageActionData messageActionData -> new MessageActionEditor(messageActionData, adventureData);
            case DestroyActionData destroyActionData -> new DestroyActionEditor(destroyActionData, adventureData);
            case RemoveActionData removeActionData -> new RemoveActionEditor(removeActionData, adventureData);
            case IncrementVariableActionData incrementActionData ->
                    new IncrementVariableActionEditor(incrementActionData);
            case DecrementVariableActionData decrementActionData ->
                    new DecrementVariableActionEditor(decrementActionData);
            case DescribeActionData describeActionData -> new DescribeActionEditor(describeActionData, adventureData);
            case CreateActionData createActionData -> new CreateActionEditor(createActionData, adventureData);
            case InventoryActionData inventoryActionData -> new InventoryActionEditor(inventoryActionData);
            case TakeActionData takeActionData -> new TakeActionEditor(takeActionData, adventureData);
            case DropActionData dropActionData -> new DropActionEditor(dropActionData, adventureData);
            case WearActionData wearActionData -> new WearActionEditor(wearActionData, adventureData);
            default -> throw new UnsupportedOperationException(
                    "No editor available for action type: " + actionData.getClass().getSimpleName()
            );
        };

        // Initialize the UI after construction (all fields are now set)
        editor.initialize();
        return editor;
    }
}
