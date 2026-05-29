package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.pdg.adventure.model.condition.PreConditionData;

public abstract class ConditionEditorComponent extends VerticalLayout {
    protected final PreConditionData conditionData;

    protected ConditionEditorComponent(PreConditionData conditionData) {
        this.conditionData = conditionData;
        setPadding(true);
        setSpacing(true);
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
