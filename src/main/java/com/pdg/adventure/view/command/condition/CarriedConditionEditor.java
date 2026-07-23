package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.CarriedConditionData;

@AutoRegisterConditionEditor
public class CarriedConditionEditor extends AbstractSingleItemConditionEditor<CarriedConditionData> {

    public CarriedConditionEditor(CarriedConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Item", "Select item", "Please select an item");
    }

    @Override
    protected String currentItemId() { return typedCondition.getItemId(); }

    @Override
    protected void applyItemId(String id) { typedCondition.setItemId(id); }
}
