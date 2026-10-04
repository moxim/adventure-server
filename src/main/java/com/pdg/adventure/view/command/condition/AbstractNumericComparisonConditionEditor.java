package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.IntegerField;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

public abstract class AbstractNumericComparisonConditionEditor<T extends PreConditionData>
        extends ConditionEditorComponent<T> {

    protected final T typedCondition;
    private final String operator;
    private VariableNameSelector variableNameField;
    private final AdventureData adventureData;
    private IntegerField valueField;

    protected AbstractNumericComparisonConditionEditor(T conditionData, String operator,
                                                       AdventureData anAdventureData) {
        super(conditionData);
        this.typedCondition = conditionData;
        this.operator = operator;
        this.adventureData = anAdventureData;
    }

    protected abstract String currentVariableName();

    protected abstract void applyVariableName(String name);

    protected abstract Integer currentValue();

    protected abstract void applyValue(Integer value);

    @Override
    protected final void buildUI() {
        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false, currentVariableName());

        valueField = new IntegerField("Value (number)");
        valueField.setWidthFull();
        valueField.setRequired(true);

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
        boolean nameValid = variableNameField.validateSelection();
        boolean valValid = false;
        if (valueField.getValue() != null) {
            try { valueField.getValue(); valValid = true; }
            catch (NumberFormatException ignored) {}
        }
        valueField.setInvalid(!valValid);
        if (!valValid) valueField.setErrorMessage("Please enter a valid number");
        return nameValid && valValid;
    }

    @Override
    public final String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.selectedName().isEmpty())
                ? variableNameField.selectedName() : "";
        String val = (valueField != null && valueField.getValue() != null)
                ? valueField.getValue().toString() : "";
        if (var.isEmpty()) return "(none)";
        return var + " " + operator + " " + val;
    }
}
