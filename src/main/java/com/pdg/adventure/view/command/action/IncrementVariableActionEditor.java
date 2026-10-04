package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Editor component for IncrementVariableActionData.
 * Allows specifying a variable name and the amount to increment it by.
 */
@AutoRegisterActionEditor
public class IncrementVariableActionEditor extends ActionEditorComponent<IncrementVariableActionData> {
    private final IncrementVariableActionData incrementActionData;
    private VariableNameSelector variableNameField;
    private final AdventureData adventureData;
    private IntegerField incrementAmountField;

    public IncrementVariableActionEditor(IncrementVariableActionData actionData) {
        this(actionData, null);
    }

    public IncrementVariableActionEditor(IncrementVariableActionData actionData, AdventureData anAdventureData) {
        super(actionData);
        this.incrementActionData = actionData;
        this.adventureData = anAdventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Increment Variable Action");
        Span description = new Span("Increment a named variable by the specified amount");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false,
                incrementActionData.getName());
        variableNameField.setPlaceholder("Select a variable");
        variableNameField.setHelperText("Only defined variables can be chosen - a Set Variable action defines one.");

        incrementAmountField = new IntegerField("Increment Amount");
        incrementAmountField.setPlaceholder("Enter increment amount");
        incrementAmountField.setWidthFull();
        incrementAmountField.setRequired(true);

        // Pre-populate if data fields are already set
        if (incrementActionData.getValue() != null) {
            incrementAmountField.setValue(incrementActionData.getValue());
        }

        // Write back to actionData on value change
        variableNameField.addValueChangeListener(e -> incrementActionData.setName(e.getValue()));
        incrementAmountField.addValueChangeListener(e -> incrementActionData.setValue(e.getValue()));

        add(title, description, variableNameField, incrementAmountField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.validateSelection();
        boolean valueValid = incrementAmountField.getValue() != null;

        if (!valueValid) {
            incrementAmountField.setErrorMessage("Please enter an increment amount");
            incrementAmountField.setInvalid(true);
        } else {
            incrementAmountField.setInvalid(false);
        }

        return nameValid && valueValid;
    }

    @Override
    public String getActionSummary() {
        String name = (variableNameField != null && !variableNameField.selectedName().isEmpty())
                ? variableNameField.selectedName() : "";
        String amount = (incrementAmountField != null && incrementAmountField.getValue() != null)
                ? incrementAmountField.getValue().toString() : "";
        if (name.isEmpty()) return "(none)";
        return name + " += " + amount;
    }
}
