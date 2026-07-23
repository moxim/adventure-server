package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.PreConditionData;

/**
 * Factory for creating condition editor components.
 * The concrete editor for a given condition data type is discovered via
 * {@link ConditionEditorRegistry}, which scans for {@link AutoRegisterConditionEditor}-annotated
 * {@link ConditionEditorComponent} implementations. To add support for a new condition type, write
 * the editor class and annotate it - no changes needed here.
 */
public class ConditionEditorFactory {

    public static ConditionEditorComponent createEditor(PreConditionData data, AdventureData adventureData) {
        Class<? extends ConditionEditorComponent<?>> editorClass = ConditionEditorRegistry.editorClassFor(data.getClass());
        if (editorClass == null) {
            throw new UnsupportedOperationException(
                    "No editor available for condition type: " + data.getClass().getSimpleName());
        }

        ConditionEditorComponent<?> editor = ConditionEditorRegistry.instantiate(editorClass, data, adventureData);
        editor.initialize();
        return editor;
    }
}
