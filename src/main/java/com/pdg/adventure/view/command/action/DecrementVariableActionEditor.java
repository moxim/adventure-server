package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.action.DecrementVariableActionData;

/**
 * Editor component for DecrementVariableActionData.
 * Allows specifying the variable name and the amount to decrement it by.
 */
@AutoRegisterActionEditor
public class DecrementVariableActionEditor extends ActionEditorComponent<DecrementVariableActionData> {
    private final DecrementVariableActionData actionData;
    private TextField nameField;
    private TextField valueField;

    public DecrementVariableActionEditor(DecrementVariableActionData actionData) {
        super(actionData);
        this.actionData = actionData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Decrement Variable Action");
        Span description = new Span("Decrement a named variable by the specified amount");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        nameField = new TextField("Variable Name");
        nameField.setPlaceholder("Enter variable name");
        nameField.setWidthFull();
        nameField.setRequired(true);

        valueField = new TextField("Decrement Amount");
        valueField.setPlaceholder("Enter decrement amount");
        valueField.setWidthFull();
        valueField.setRequired(true);

        // Pre-populate fields if actionData already has values
        if (actionData.getName() != null) {
            nameField.setValue(actionData.getName());
        }
        if (actionData.getValue() != null) {
            valueField.setValue(actionData.getValue());
        }

        // Write back to actionData on change
        nameField.addValueChangeListener(e -> actionData.setName(e.getValue()));
        valueField.addValueChangeListener(e -> actionData.setValue(e.getValue()));

        add(title, description, nameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = nameField.getValue() != null && !nameField.getValue().isBlank();
        boolean valueValid = valueField.getValue() != null && !valueField.getValue().isBlank();

        nameField.setInvalid(!nameValid);
        if (!nameValid) {
            nameField.setErrorMessage("Please enter a variable name");
        }

        valueField.setInvalid(!valueValid);
        if (!valueValid) {
            valueField.setErrorMessage("Please enter a decrement amount");
        }

        return nameValid && valueValid;
    }

    @Override
    public String getActionSummary() {
        String name = (nameField != null && !nameField.getValue().isEmpty())
                ? nameField.getValue() : "";
        String amount = (valueField != null && !valueField.getValue().isEmpty())
                ? valueField.getValue() : "";
        if (name.isEmpty()) return "(none)";
        return name + " -= " + amount;
    }
}
