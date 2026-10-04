package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;

@AutoRegisterConditionEditor
public class GreaterThanConditionEditor extends AbstractNumericComparisonConditionEditor<GreaterThanConditionData> {

    public GreaterThanConditionEditor(GreaterThanConditionData conditionData) {
        this(conditionData, null);
    }

    public GreaterThanConditionEditor(GreaterThanConditionData conditionData, AdventureData anAdventureData) {
        super(conditionData, ">", anAdventureData);
    }

    @Override
    protected String currentVariableName() { return typedCondition.getVariableName(); }

    @Override
    protected void applyVariableName(String name) { typedCondition.setVariableName(name); }

    @Override
    protected Integer currentValue() { return typedCondition.getValue(); }

    @Override
    protected void applyValue(Integer value) { typedCondition.setValue(value); }
}
