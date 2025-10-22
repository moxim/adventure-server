package com.pdg.adventure.views.commands.actions;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.pdg.adventure.model.action.ActionData;

/**
 * Base abstract class for all action editors.
 * Each action type should extend this class and implement the buildUI method
 * to create its specific user interface.
 *
 * Note: The UI is not built in the constructor. After construction, call initialize()
 * to trigger the UI building. This allows subclasses to set their fields before buildUI() is called.
 */
public abstract class ActionEditorComponent extends VerticalLayout {
    protected final ActionData actionData;

    protected ActionEditorComponent(ActionData actionData) {
        this.actionData = actionData;
        setPadding(true);
        setSpacing(true);
        // DON'T call buildUI() here - let subclass set its fields first
    }

    /**
     * Initialize the UI. This should be called after construction to trigger buildUI().
     * This method is final to ensure the initialization sequence is consistent.
     */
    public final void initialize() {
        buildUI();
    }

    /**
     * Template method - each subclass must implement this to build its specific UI.
     */
    protected abstract void buildUI();

    /**
     * Get the action data managed by this editor.
     *
     * @return the action data
     */
    public ActionData getActionData() {
        return actionData;
    }

    /**
     * Validate the action data.
     *
     * @return true if the action data is valid, false otherwise
     */
    public abstract boolean validate();

    /**
     * Get a human-readable name for this action type.
     *
     * @return the action type name
     */
    public String getActionTypeName() {
        return actionData.getActionName();
    }
}
