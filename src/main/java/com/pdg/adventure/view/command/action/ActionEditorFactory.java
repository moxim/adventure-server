package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;

/**
 * Factory for creating action editor components.
 * The concrete editor for a given action data type is discovered via {@link ActionEditorRegistry},
 * which scans for {@link AutoRegisterActionEditor}-annotated {@link ActionEditorComponent}
 * implementations. To add support for a new action type, write the editor class and annotate it -
 * no changes needed here.
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
        Class<? extends ActionEditorComponent<?>> editorClass = ActionEditorRegistry.editorClassFor(actionData.getClass());
        if (editorClass == null) {
            throw new UnsupportedOperationException(
                    "No editor available for action type: " + actionData.getClass().getSimpleName());
        }

        ActionEditorComponent<?> editor = ActionEditorRegistry.instantiate(editorClass, actionData, adventureData);

        // Initialize the UI after construction (all fields are now set)
        editor.initialize();
        return editor;
    }
}
