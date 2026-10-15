package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DiagnoseVariableActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Editor component for DiagnoseVariableActionData.
 * Allows choosing the variable whose name and value the game prints, e.g. "VISITED : 14".
 */
@AutoRegisterActionEditor
public class DiagnoseVariableActionEditor extends ActionEditorComponent<DiagnoseVariableActionData> {
    private final DiagnoseVariableActionData diagnoseActionData;
    private final AdventureData adventureData;
    private VariableNameSelector variableNameField;

    public DiagnoseVariableActionEditor(DiagnoseVariableActionData actionData) {
        this(actionData, null);
    }

    public DiagnoseVariableActionEditor(DiagnoseVariableActionData actionData, AdventureData anAdventureData) {
        super(actionData);
        this.diagnoseActionData = actionData;
        this.adventureData = anAdventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Diagnose Variable Action");
        Span description = new Span("Print a variable's name and value, for example VISITED : 14");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false,
                diagnoseActionData.getName());
        variableNameField.setPlaceholder("Select a variable");
        variableNameField.setHelperText("Only defined variables can be chosen - a Set Variable action defines one.");

        variableNameField.addValueChangeListener(e -> diagnoseActionData.setName(e.getValue()));

        add(title, description, variableNameField);
    }

    @Override
    public boolean validate() {
        return variableNameField.validateSelection();
    }

    @Override
    public String getActionSummary() {
        String name = variableNameField == null ? "" : variableNameField.selectedName();
        return name.isEmpty() ? "(none)" : name;
    }
}
