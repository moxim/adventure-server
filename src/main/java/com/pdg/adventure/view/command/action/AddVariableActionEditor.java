package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.AddVariableActionData;

/**
 * Editor component for AddVariableActionData.
 */
@AutoRegisterActionEditor
public class AddVariableActionEditor extends AbstractTwoVariablesActionEditor<AddVariableActionData> {

    public AddVariableActionEditor(AddVariableActionData anActionData) {
        this(anActionData, null);
    }

    public AddVariableActionEditor(AddVariableActionData anActionData, AdventureData anAdventureData) {
        super(anActionData, anAdventureData);
    }

    @Override
    String title() {
        return "Add Variable Action";
    }

    @Override
    String descriptionText() {
        return "Add the value of the first variable to the second";
    }

    @Override
    String operator() {
        return "+=";
    }

    @Override
    String firstName() {
        return actionData.getFirstName();
    }

    @Override
    String secondName() {
        return actionData.getSecondName();
    }

    @Override
    void setFirstName(String aName) {
        actionData.setFirstName(aName);
    }

    @Override
    void setSecondName(String aName) {
        actionData.setSecondName(aName);
    }
}
