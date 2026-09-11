package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.PreConditionData;

public abstract class AbstractNumericComparisonConditionEditor<T extends PreConditionData>
        extends ConditionEditorComponent<T> {

    protected final T typedCondition;
    private final String operator;
    private TextField variableNameField;
    private IntegerField valueField;

    protected AbstractNumericComparisonConditionEditor(T conditionData, String operator) {
        super(conditionData);
        this.typedCondition = conditionData;
        this.operator = operator;
    }

    protected abstract String currentVariableName();

    protected abstract void applyVariableName(String name);

    protected abstract Integer currentValue();

    protected abstract void applyValue(Integer value);

    @Override
    protected final void buildUI() {
        variableNameField = new TextField("Variable Name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        valueField = new IntegerField("Value (number)");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (currentVariableName() != null) variableNameField.setValue(currentVariableName());
        if (currentValue() != null) valueField.setValue(currentValue());

        variableNameField.addValueChangeListener(e -> applyVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> {
            try { applyValue(e.getValue()); }
            catch (NumberFormatException ex) { applyValue(null); }
        });

        add(variableNameField, valueField);
    }

    @Override
    public final boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valValid = false;
        if (valueField.getValue() != null) {
            try { valueField.getValue(); valValid = true; }
            catch (NumberFormatException ignored) {}
        }
        variableNameField.setInvalid(!nameValid);
        valueField.setInvalid(!valValid);
        if (!nameValid) variableNameField.setErrorMessage("Please enter a variable name");
        if (!valValid) valueField.setErrorMessage("Please enter a valid number");
        return nameValid && valValid;
    }

    @Override
    public final String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : "";
        String val = (valueField != null && valueField.getValue() != null)
                ? valueField.getValue().toString() : "";
        if (var.isEmpty()) return "(none)";
        return var + " " + operator + " " + val;
    }
}
