package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Optional;

import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.view.support.SelectBoxFocus;

/**
 * Base abstract class for all condition editors.
 * Each condition type should extend this class and implement buildUI()
 * to create its specific user interface.
 * <p>
 * Note: The UI is not built in the constructor. After construction, call initialize()
 * to trigger the UI building. This allows subclasses to set their fields before buildUI() is called.
 */
public abstract class ConditionEditorComponent<D extends PreConditionData> extends VerticalLayout {
    protected final D conditionData;

    protected ConditionEditorComponent(D conditionData) {
        this.conditionData = conditionData;
        setPadding(true);
        setSpacing(true);
        // DON'T call buildUI() here - let subclass set its fields first
    }

    public final void initialize() {
        buildUI();
    }

    protected abstract void buildUI();

    public D getConditionData() {
        return conditionData;
    }

    /**
     * Puts the cursor into the editor's first select box and scrolls it into view, so the author can start choosing
     * right away. Does nothing for an editor without a select box.
     */
    public void focusFirstSelectBox() {
        SelectBoxFocus.focusFirstIn(this);
    }

    /** The first select box in this editor, in display order. */
    Optional<Component> firstSelectBox() {
        return SelectBoxFocus.firstIn(this);
    }

    public abstract boolean validate();

    public abstract String getConditionSummary();
}
