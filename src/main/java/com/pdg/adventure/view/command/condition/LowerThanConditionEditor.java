package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.condition.LowerThanConditionData;

public class LowerThanConditionEditor extends AbstractNumericComparisonConditionEditor<LowerThanConditionData> {

    public LowerThanConditionEditor(LowerThanConditionData conditionData) {
        super(conditionData, "<");
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
