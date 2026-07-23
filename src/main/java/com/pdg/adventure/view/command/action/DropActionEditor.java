package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DropActionData;

@AutoRegisterActionEditor
public class DropActionEditor extends AbstractSingleItemActionEditor<DropActionData> {

    public DropActionEditor(DropActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Drop Action",
              "Select the item the player will drop.",
              "Item to Drop", "Select item to drop", "Please select an item to drop");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
