package com.pdg.adventure.views.commands.actions;

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
        ActionEditorComponent editor;

        if (actionData instanceof MovePlayerActionData) {
            editor = new MovePlayerActionEditor((MovePlayerActionData) actionData, adventureData);
        } else if (actionData instanceof MoveItemActionData) {
            editor = new MoveItemActionEditor((MoveItemActionData) actionData, adventureData);
        } else if (actionData instanceof MessageActionData) {
            editor = new MessageActionEditor((MessageActionData) actionData, adventureData);
        }
        // TODO: Add more action types as they are implemented
        // else if (actionData instanceof SetVariableActionData) {
        //     editor = new SetVariableActionEditor((SetVariableActionData) actionData);
        // }
        // ... more action types
        else {
            throw new UnsupportedOperationException(
                    "No editor available for action type: " + actionData.getClass().getSimpleName()
            );
        }

        // Initialize the UI after construction (all fields are now set)
        editor.initialize();
        return editor;
    }
}
