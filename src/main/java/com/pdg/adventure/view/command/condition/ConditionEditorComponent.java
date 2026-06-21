package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.pdg.adventure.model.condition.PreConditionData;

/**
 * Base abstract class for all condition editors.
 * Each condition type should extend this class and implement buildUI()
 * to create its specific user interface.
 * <p>
 * Note: The UI is not built in the constructor. After construction, call initialize()
 * to trigger the UI building. This allows subclasses to set their fields before buildUI() is called.
 */
public abstract class ConditionEditorComponent extends VerticalLayout {
    protected final PreConditionData conditionData;

    protected ConditionEditorComponent(PreConditionData conditionData) {
        this.conditionData = conditionData;
        setPadding(true);
        setSpacing(true);
        // DON'T call buildUI() here - let subclass set its fields first
    }

    public final void initialize() {
        buildUI();
    }

    protected abstract void buildUI();

    public PreConditionData getConditionData() {
        return conditionData;
    }

    public abstract boolean validate();

    public abstract String getConditionSummary();
}
