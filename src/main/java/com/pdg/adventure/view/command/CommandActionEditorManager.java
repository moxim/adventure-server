package com.pdg.adventure.view.command;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.view.command.action.ActionEditorComponent;
import com.pdg.adventure.view.command.action.ActionEditorFactory;
import com.pdg.adventure.view.command.action.ActionSelector;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the action editor UI component for command editing.
 * This class encapsulates all ActionEditor-related functionality including:
 * - Displaying action editors for different action types
 * - Handling action selection and changes
 * - Tracking modifications and validation state
 * - Reset functionality
 */
public class CommandActionEditorManager {
    private final VerticalLayout actionEditorContainer;
    private final AdventureData adventureData;
    private transient ActionEditorComponent actionEditor;
    private transient ActionData originalActionData;
    private boolean actionEditorHasChanges = false;
    private final List<Consumer<ChangeEvent>> changeListeners = new ArrayList<>();

    /**
     * Event object for notifying listeners of changes to the action editor.
     */
    public static class ChangeEvent {
        private final boolean hasChanges;
        private final boolean isValid;

        public ChangeEvent(boolean hasChanges, boolean isValid) {
            this.hasChanges = hasChanges;
            this.isValid = isValid;
        }

        public boolean hasChanges() {
            return hasChanges;
        }

        public boolean isValid() {
            return isValid;
        }
    }

    /**
     * Constructor
     * @param adventureData The adventure data containing available actions and vocabulary
     */
    public CommandActionEditorManager(AdventureData adventureData) {
        this.adventureData = adventureData;
        this.actionEditorContainer = new VerticalLayout();
        this.actionEditorContainer.setPadding(false);
        this.actionEditorContainer.setSpacing(true);
    }

    /**
     * Get the container that holds the action editor UI.
     * This should be added to the parent layout.
     */
    public VerticalLayout getContainer() {
        return actionEditorContainer;
    }

    /**
     * Set up the action editor for a specific command.
     * @param action The action data to edit (may be null for new commands)
     */
    public void setup(ActionData action) {
        // Store the original action data for reset functionality
        originalActionData = action;
        actionEditorHasChanges = false;

        // Show the action editor for the specified action
        showActionEditorForCommand(action);
    }

    /**
     * Show the action editor for a specific action.
     * Used when selecting a command from the grid.
     * @param action The action to display (may be null)
     */
    private void showActionEditorForCommand(ActionData action) {
        actionEditorContainer.removeAll();

        // If there's an action, show its editor
        if (action != null) {
            try {
                actionEditor = ActionEditorFactory.createEditor(action, adventureData);

                // Add a "Change Action" button above the editor
                Button changeActionButton = new Button("Change Action");
                changeActionButton.addClickListener(e -> showActionSelector());

                actionEditorContainer.add(changeActionButton, actionEditor);

                // Attach listeners to update state when action editor fields change
                attachActionEditorListeners(actionEditor);
            } catch (UnsupportedOperationException e) {
                // Action type not supported yet, show a message and the selector
                Div message = new Div();
                message.setText("Action editor not available for: " + action.getActionName());
                message.getStyle().set("color", "var(--lumo-error-text-color)");
                actionEditorContainer.add(message);
                showActionSelector();
            }
        } else {
            // No action yet, show the action selector
            showActionSelector();
        }
    }

    /**
     * Reset the action editor to its original state.
     * This is called when the reset button is clicked.
     */
    public void reset() {
        actionEditorContainer.removeAll();

        // If there was an original action, recreate its editor
        if (originalActionData != null) {
            try {
                actionEditor = ActionEditorFactory.createEditor(originalActionData, adventureData);

                // Add a "Change Action" button above the editor
                Button changeActionButton = new Button("Change Action");
                changeActionButton.addClickListener(e -> showActionSelector());

                actionEditorContainer.add(changeActionButton, actionEditor);

                // Attach listeners to update state when action editor fields change
                attachActionEditorListeners(actionEditor);
            } catch (UnsupportedOperationException e) {
                // Action type not supported yet, show a message and the selector
                Div message = new Div();
                message.setText("Action editor not available for: " + originalActionData.getActionName());
                message.getStyle().set("color", "var(--lumo-error-text-color)");
                actionEditorContainer.add(message);
                showActionSelector();
            }
        } else {
            // No original action, show the action selector
            actionEditor = null;
            showActionSelector();
        }

        // Reset change tracking
        actionEditorHasChanges = false;
        notifyChangeListeners();
    }

    /**
     * Show the action selector UI for choosing a new action type.
     */
    private void showActionSelector() {
        actionEditorContainer.removeAll();

        ActionSelector actionSelector = new ActionSelector(adventureData);
        actionSelector.setEditorSelectedListener(editor -> {
            // Replace the selector with the selected editor
            actionEditorContainer.removeAll();
            actionEditor = editor;

            // Add a "Change Action" button above the new editor
            Button changeActionButton = new Button("Change Action");
            changeActionButton.addClickListener(e -> showActionSelector());

            actionEditorContainer.add(changeActionButton, actionEditor);

            // Attach listeners to update state when action editor fields change
            attachActionEditorListeners(actionEditor);

            // Mark that the action has changed since a new action type was selected
            actionEditorHasChanges = true;

            // Notify listeners of the change
            notifyChangeListeners();
        });
        actionEditorContainer.add(actionSelector);
    }

    /**
     * Attach validation listeners to the action editor's input components.
     * This ensures change notifications are sent when action editor fields change.
     * @param editor The action editor to attach listeners to
     */
    private void attachActionEditorListeners(ActionEditorComponent editor) {
        if (editor == null) {
            return;
        }

        // Add a value change listener to all input components in the editor
        editor.getChildren().forEach(component -> {
            if (component instanceof com.vaadin.flow.component.HasValue) {
                ((com.vaadin.flow.component.HasValue<?, ?>) component).addValueChangeListener(e -> {
                    // Mark that the action editor has changes
                    if (!e.isFromClient()) {
                        // This is a programmatic change (initial value setting), don't mark as changed
                        return;
                    }
                    actionEditorHasChanges = true;
                    notifyChangeListeners();
                });
            }
        });
    }

    /**
     * Validate the current action editor.
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        return actionEditor == null || actionEditor.validate();
    }

    /**
     * Check if the action editor has unsaved changes.
     * @return true if there are unsaved changes
     */
    public boolean hasChanges() {
        return actionEditorHasChanges;
    }

    /**
     * Get the current action data from the editor.
     * @return The action data, or null if no action is set
     */
    public ActionData getActionData() {
        if (actionEditor != null) {
            return actionEditor.getActionData();
        }
        return null;
    }

    /**
     * Update the original action data to reflect a successful save.
     * This allows subsequent resets to restore to the newly saved state.
     */
    public void updateOriginalActionData() {
        if (actionEditor != null) {
            originalActionData = actionEditor.getActionData();
        }
        actionEditorHasChanges = false;
    }

    /**
     * Add a listener that will be notified when the action editor state changes.
     * @param listener Consumer that receives ChangeEvent notifications
     */
    public void addChangeListener(Consumer<ChangeEvent> listener) {
        changeListeners.add(listener);
    }

    /**
     * Remove a previously registered change listener.
     * @param listener The listener to remove
     */
    public void removeChangeListener(Consumer<ChangeEvent> listener) {
        changeListeners.remove(listener);
    }

    /**
     * Notify all registered listeners of a change in the action editor state.
     */
    private void notifyChangeListeners() {
        boolean isValid = validate();
        ChangeEvent event = new ChangeEvent(actionEditorHasChanges, isValid);
        changeListeners.forEach(listener -> listener.accept(event));
    }
}
