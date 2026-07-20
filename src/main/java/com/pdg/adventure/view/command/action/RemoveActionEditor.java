package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.RemoveActionData;

@AutoRegisterActionEditor
public class RemoveActionEditor extends AbstractSingleItemActionEditor<RemoveActionData> {

    public RemoveActionEditor(RemoveActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Remove Action",
              "Remove (un-wear) a wearable item from the player. Shows all items; ensure you select a wearable one.",
              "Item to Remove", "Select item to remove", "Please select an item to remove");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
