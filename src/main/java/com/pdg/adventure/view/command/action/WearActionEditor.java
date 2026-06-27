package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.WearActionData;

public class WearActionEditor extends AbstractSingleItemActionEditor<WearActionData> {

    public WearActionEditor(WearActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Wear Action",
              "Select the wearable item the player will wear. Shows all items; ensure you select a wearable one.",
              "Item to Wear", "Select item to wear", "Please select an item to wear");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
