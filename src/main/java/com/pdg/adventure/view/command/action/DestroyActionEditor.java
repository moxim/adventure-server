package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DestroyActionData;

public class DestroyActionEditor extends AbstractSingleItemActionEditor<DestroyActionData> {

    public DestroyActionEditor(DestroyActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Destroy Action",
              "Select the item to remove permanently from the adventure.",
              "Item to Destroy", "Select item to destroy", "Please select an item to destroy");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
