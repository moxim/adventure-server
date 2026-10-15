package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.MesActionData;

/**
 * Editor component for MesActionData, which the authors know as "Msg": a message from the message catalog that is
 * not followed by a line break.
 */
@AutoRegisterActionEditor
public class MesActionEditor extends AbstractMessageActionEditor<MesActionData> {

    public MesActionEditor(MesActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData);
    }

    @Override
    String title() {
        return "Msg Action";
    }

    @Override
    String descriptionText() {
        return "Display a message without ending the line, so the next text continues on the same line";
    }

    @Override
    String messageId() {
        return actionData.getMessageId();
    }

    @Override
    void setMessageId(String aMessageId) {
        actionData.setMessageId(aMessageId);
    }

    /** Shown to the authors in the action row; "Mes" is only the engine's name. */
    @Override
    public String getActionTypeName() {
        return "MsgActionData";
    }
}
