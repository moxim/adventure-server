package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.PrintVariableActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Editor component for PrintVariableActionData.
 * Allows choosing the variable whose name and value the game prints, e.g. "VISITED : 14".
 */
@AutoRegisterActionEditor
public class PrintVariableActionEditor extends ActionEditorComponent<PrintVariableActionData> {
    private final PrintVariableActionData printActionData;
    private final AdventureData adventureData;
    private VariableNameSelector variableNameField;

    public PrintVariableActionEditor(PrintVariableActionData actionData) {
        this(actionData, null);
    }

    public PrintVariableActionEditor(PrintVariableActionData actionData, AdventureData anAdventureData) {
        super(actionData);
        this.printActionData = actionData;
        this.adventureData = anAdventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Print Variable Action");
        Span description = new Span("Print a variable's name and value, for example VISITED : 14");
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
