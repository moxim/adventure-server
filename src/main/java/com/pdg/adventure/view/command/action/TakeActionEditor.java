package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.TakeActionData;

@AutoRegisterActionEditor
public class TakeActionEditor extends AbstractSingleItemActionEditor<TakeActionData> {

    public TakeActionEditor(TakeActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Take Action",
              "Select the item the player will pick up.",
              "Item to Take", "Select item to take", "Please select an item to take");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
