package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.action.IncrementVariableActionData;

/**
 * Editor component for IncrementVariableActionData.
 * Allows specifying a variable name and the amount to increment it by.
 */
public class IncrementVariableActionEditor extends ActionEditorComponent {
    private final IncrementVariableActionData incrementActionData;
    private TextField variableNameField;
    private TextField incrementAmountField;

    public IncrementVariableActionEditor(IncrementVariableActionData actionData) {
        super(actionData);
        this.incrementActionData = actionData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Increment Variable Action");
        Span description = new Span("Increment a named variable by the specified amount");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new TextField("Variable Name");
        variableNameField.setPlaceholder("Enter variable name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        incrementAmountField = new TextField("Increment Amount");
        incrementAmountField.setPlaceholder("Enter increment amount");
        incrementAmountField.setWidthFull();
        incrementAmountField.setRequired(true);

        // Pre-populate if data fields are already set
        if (incrementActionData.getName() != null) {
            variableNameField.setValue(incrementActionData.getName());
        }
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
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valueValid = incrementAmountField.getValue() != null && !incrementAmountField.getValue().trim().isEmpty();

        if (!nameValid) {
            variableNameField.setErrorMessage("Please enter a variable name");
            variableNameField.setInvalid(true);
        } else {
            variableNameField.setInvalid(false);
        }

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
        String name = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : "";
        String amount = (incrementAmountField != null && !incrementAmountField.getValue().isEmpty())
                ? incrementAmountField.getValue() : "";
        if (name.isEmpty()) return "(none)";
        return name + " += " + amount;
    }
}
