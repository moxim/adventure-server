package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;

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
            // TODO: Add more action types as they are implemented
            // else if (actionData instanceof SetVariableActionData) {
            //     editor = new SetVariableActionEditor((SetVariableActionData) actionData);
            // }
            // ... more action types
            default -> throw new UnsupportedOperationException(
                    "No editor available for action type: " + actionData.getClass().getSimpleName()
            );
        };

        // Initialize the UI after construction (all fields are now set)
        editor.initialize();
        return editor;
    }
}
