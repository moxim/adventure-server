package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.IntegerField;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

@AutoRegisterConditionEditor
public class EqualsConditionEditor extends ConditionEditorComponent<EqualsConditionData> {
    private final EqualsConditionData equalsData;
    private VariableNameSelector variableNameField;
    private final AdventureData adventureData;
    private IntegerField valueField;

    public EqualsConditionEditor(EqualsConditionData conditionData) {
        this(conditionData, null);
    }

    public EqualsConditionEditor(EqualsConditionData conditionData, AdventureData anAdventureData) {
        super(conditionData);
        this.equalsData = conditionData;
        this.adventureData = anAdventureData;
    }

    @Override
    protected void buildUI() {
        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false,
                equalsData.getVariableName());

        valueField = new IntegerField("Value");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (equalsData.getValue() != null) valueField.setValue(equalsData.getValue());

        variableNameField.addValueChangeListener(e -> equalsData.setVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> equalsData.setValue(e.getValue()));

        add(variableNameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.validateSelection();
        boolean valValid = valueField.getValue() != null;
        valueField.setInvalid(!valValid);
        if (!valValid) valueField.setErrorMessage("Please enter a value");
        return nameValid && valValid;
    }

    @Override
    public String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.selectedName().isEmpty())
                ? variableNameField.selectedName() : (equalsData.getVariableName() != null ? equalsData.getVariableName() : "");
        String val = (valueField != null)
                ? String.valueOf(valueField.getValue()) : (equalsData.getValue() != null ? String.valueOf(equalsData.getValue()) : "");
        if (var.isEmpty()) return "(none)";
        return var + " = " + val;
    }
}
