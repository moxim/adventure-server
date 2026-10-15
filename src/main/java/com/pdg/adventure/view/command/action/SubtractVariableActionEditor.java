package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.SubtractVariableActionData;

/**
 * Editor component for SubtractVariableActionData.
 */
@AutoRegisterActionEditor
public class SubtractVariableActionEditor extends AbstractTwoVariablesActionEditor<SubtractVariableActionData> {

    public SubtractVariableActionEditor(SubtractVariableActionData anActionData) {
        this(anActionData, null);
    }

    public SubtractVariableActionEditor(SubtractVariableActionData anActionData, AdventureData anAdventureData) {
        super(anActionData, anAdventureData);
    }

    @Override
    String title() {
        return "Subtract Variable Action";
    }

    @Override
    String descriptionText() {
        return "Subtract the value of the first variable from the second";
    }

    @Override
    String operator() {
        return "-=";
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
