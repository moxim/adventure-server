package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.EqualsConditionData;

@AutoRegisterConditionEditor
public class EqualsConditionEditor extends ConditionEditorComponent<EqualsConditionData> {
    private final EqualsConditionData equalsData;
    private TextField variableNameField;
    private TextField valueField;

    public EqualsConditionEditor(EqualsConditionData conditionData) {
        super(conditionData);
        this.equalsData = conditionData;
    }

    @Override
    protected void buildUI() {
        variableNameField = new TextField("Variable Name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        valueField = new TextField("Value");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (equalsData.getVariableName() != null) variableNameField.setValue(equalsData.getVariableName());
        if (equalsData.getValue() != null) valueField.setValue(equalsData.getValue());

        variableNameField.addValueChangeListener(e -> equalsData.setVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> equalsData.setValue(e.getValue()));

        add(variableNameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valValid = valueField.getValue() != null && !valueField.getValue().trim().isEmpty();
        variableNameField.setInvalid(!nameValid);
        valueField.setInvalid(!valValid);
        if (!nameValid) variableNameField.setErrorMessage("Please enter a variable name");
        if (!valValid) valueField.setErrorMessage("Please enter a value");
        return nameValid && valValid;
    }

    @Override
    public String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : (equalsData.getVariableName() != null ? equalsData.getVariableName() : "");
        String val = (valueField != null && !valueField.getValue().isEmpty())
                ? valueField.getValue() : (equalsData.getValue() != null ? equalsData.getValue() : "");
        if (var.isEmpty()) return "(none)";
        return var + " = " + val;
    }
}
