package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.HereConditionData;

public class HereConditionEditor extends AbstractSingleItemConditionEditor<HereConditionData> {

    public HereConditionEditor(HereConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Item", "Select item", "Please select an item");
    }

    @Override
    protected String currentItemId() { return typedCondition.getThingId(); }

    @Override
    protected void applyItemId(String id) { typedCondition.setThingId(id); }
}
