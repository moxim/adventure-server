package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.PrintActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Editor component for PrintActionData.
 * Allows choosing the variable whose value the game prints, e.g. "14", without ending the line.
 */
@AutoRegisterActionEditor
public class PrintActionEditor extends ActionEditorComponent<PrintActionData> {
    private final PrintActionData printActionData;
    private final AdventureData adventureData;
    private VariableNameSelector variableNameField;

    public PrintActionEditor(PrintActionData actionData) {
        this(actionData, null);
    }

    public PrintActionEditor(PrintActionData actionData, AdventureData anAdventureData) {
        super(actionData);
        this.printActionData = actionData;
        this.adventureData = anAdventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Print Action");
        Span description = new Span("Print a variable's value, for example 14, without ending the line");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false,
                printActionData.getName());
        variableNameField.setPlaceholder("Select a variable");
        variableNameField.setHelperText("Only defined variables can be chosen - a Set Variable action defines one.");

        variableNameField.addValueChangeListener(e -> printActionData.setName(e.getValue()));

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
