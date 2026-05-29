package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.LowerThanConditionData;

public class LowerThanConditionEditor extends ConditionEditorComponent {
    private final LowerThanConditionData condData;
    private TextField variableNameField;
    private TextField valueField;

    public LowerThanConditionEditor(LowerThanConditionData conditionData) {
        super(conditionData);
        this.condData = conditionData;
    }

    @Override
    protected void buildUI() {
        variableNameField = new TextField("Variable Name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        valueField = new TextField("Value (number)");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (condData.getVariableName() != null) variableNameField.setValue(condData.getVariableName());
        if (condData.getValue() != null) valueField.setValue(condData.getValue().toString());

        variableNameField.addValueChangeListener(e -> condData.setVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> {
            try { condData.setValue(Double.parseDouble(e.getValue())); }
            catch (NumberFormatException ex) { condData.setValue(null); }
        });

        add(variableNameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valValid = false;
        if (valueField.getValue() != null && !valueField.getValue().trim().isEmpty()) {
            try { Double.parseDouble(valueField.getValue()); valValid = true; }
            catch (NumberFormatException ignored) {}
        }
        variableNameField.setInvalid(!nameValid);
        valueField.setInvalid(!valValid);
        if (!nameValid) variableNameField.setErrorMessage("Please enter a variable name");
        if (!valValid) valueField.setErrorMessage("Please enter a valid number");
        return nameValid && valValid;
    }

    @Override
    public String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : "";
        String val = (valueField != null && !valueField.getValue().isEmpty())
                ? valueField.getValue() : "";
        if (var.isEmpty()) return "(none)";
        return var + " < " + val;
    }
}
