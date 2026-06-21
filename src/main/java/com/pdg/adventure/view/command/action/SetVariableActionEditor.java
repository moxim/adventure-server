package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.action.SetVariableActionData;

public class SetVariableActionEditor extends ActionEditorComponent {
    private final SetVariableActionData setVariableActionData;
    private TextField variableNameField;
    private TextField variableValueField;

    public SetVariableActionEditor(SetVariableActionData actionData) {
        super(actionData);
        this.setVariableActionData = actionData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Set Variable Action");
        Span description = new Span("Set a named variable to a specific value");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new TextField("Variable Name");
        variableNameField.setPlaceholder("Enter variable name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        variableValueField = new TextField("Variable Value");
        variableValueField.setPlaceholder("Enter variable value");
        variableValueField.setWidthFull();
        variableValueField.setRequired(true);

        if (setVariableActionData.getVariableName() != null) {
            variableNameField.setValue(setVariableActionData.getVariableName());
        }
        if (setVariableActionData.getVariableValue() != null) {
            variableValueField.setValue(setVariableActionData.getVariableValue());
        }

        variableNameField.addValueChangeListener(e -> setVariableActionData.setVariableName(e.getValue()));
        variableValueField.addValueChangeListener(e -> setVariableActionData.setVariableValue(e.getValue()));

        add(title, description, variableNameField, variableValueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valueValid = variableValueField.getValue() != null && !variableValueField.getValue().trim().isEmpty();

        if (!nameValid) {
            variableNameField.setErrorMessage("Please enter a variable name");
            variableNameField.setInvalid(true);
        } else {
            variableNameField.setInvalid(false);
        }

        if (!valueValid) {
            variableValueField.setErrorMessage("Please enter a variable value");
            variableValueField.setInvalid(true);
        } else {
            variableValueField.setInvalid(false);
        }

        return nameValid && valueValid;
    }

    @Override
    public String getActionSummary() {
        String name = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : "";
        String value = (variableValueField != null && !variableValueField.getValue().isEmpty())
                ? variableValueField.getValue() : "";
        if (name.isEmpty()) return "(none)";
        return name + " = " + value;
    }
}
