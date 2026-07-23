package com.pdg.adventure.view.command.condition;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

@AutoRegisterConditionEditor
public class WornConditionEditor extends AbstractSingleItemConditionEditor<WornConditionData> {

    public WornConditionEditor(WornConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Wearable Item", "Select wearable item", "Please select a wearable item");
    }

    @Override
    protected List<ItemData> provideItems() {
        return ViewSupporter.collectAllItems(adventureData).stream()
                .filter(ItemData::isWearable).toList();
    }

    @Override
    protected String currentItemId() { return typedCondition.getThingId(); }

    @Override
    protected void applyItemId(String id) { typedCondition.setThingId(id); }
}
