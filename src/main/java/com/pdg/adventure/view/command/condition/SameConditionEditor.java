package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.SameConditionData;

public class SameConditionEditor extends ConditionEditorComponent {
    private final SameConditionData sameData;
    private TextField varOneField;
    private TextField varTwoField;

    public SameConditionEditor(SameConditionData conditionData) {
        super(conditionData);
        this.sameData = conditionData;
    }

    @Override
    protected void buildUI() {
        varOneField = new TextField("Variable 1");
        varOneField.setWidthFull();
        varOneField.setRequired(true);

        varTwoField = new TextField("Variable 2");
        varTwoField.setWidthFull();
        varTwoField.setRequired(true);

        if (sameData.getVariableNameOne() != null) varOneField.setValue(sameData.getVariableNameOne());
        if (sameData.getVariableNameTwo() != null) varTwoField.setValue(sameData.getVariableNameTwo());

        varOneField.addValueChangeListener(e -> sameData.setVariableNameOne(e.getValue()));
        varTwoField.addValueChangeListener(e -> sameData.setVariableNameTwo(e.getValue()));

        add(varOneField, varTwoField);
    }

    @Override
    public boolean validate() {
        boolean oneValid = varOneField.getValue() != null && !varOneField.getValue().trim().isEmpty();
        boolean twoValid = varTwoField.getValue() != null && !varTwoField.getValue().trim().isEmpty();
        varOneField.setInvalid(!oneValid);
        varTwoField.setInvalid(!twoValid);
        if (!oneValid) varOneField.setErrorMessage("Please enter variable 1 name");
        if (!twoValid) varTwoField.setErrorMessage("Please enter variable 2 name");
        return oneValid && twoValid;
    }

    @Override
    public String getConditionSummary() {
        String one = (varOneField != null && !varOneField.getValue().isEmpty()) ? varOneField.getValue() : "";
        String two = (varTwoField != null && !varTwoField.getValue().isEmpty()) ? varTwoField.getValue() : "";
        if (one.isEmpty()) return "(none)";
        return one + " = " + two;
    }
}
