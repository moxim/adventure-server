package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.DiagnoseVariablesActionData;

/**
 * Editor component for DiagnoseVariablesActionData.
 * The action has no configurable parameters: it prints every variable with its value, one per line.
 */
@AutoRegisterActionEditor
public class DiagnoseVariablesActionEditor extends ActionEditorComponent<DiagnoseVariablesActionData> {

    public DiagnoseVariablesActionEditor(DiagnoseVariablesActionData actionData) {
        super(actionData);
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Diagnose Variables Action");

        Span description = new Span("Print all variables with their values, one per line, for example VISITED : 14. "
                                    + "This action takes no parameters.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, description);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String getActionSummary() {
        return "";
    }
}
