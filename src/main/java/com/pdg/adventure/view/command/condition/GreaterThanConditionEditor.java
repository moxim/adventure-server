package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.condition.GreaterThanConditionData;

@AutoRegisterConditionEditor
public class GreaterThanConditionEditor extends AbstractNumericComparisonConditionEditor<GreaterThanConditionData> {

    public GreaterThanConditionEditor(GreaterThanConditionData conditionData) {
        super(conditionData, ">");
    }

    @Override
    protected String currentVariableName() { return typedCondition.getVariableName(); }

    @Override
    protected void applyVariableName(String name) { typedCondition.setVariableName(name); }

    @Override
    protected Number currentValue() { return typedCondition.getValue(); }

    @Override
    protected void applyValue(Double value) { typedCondition.setValue(value); }
}
