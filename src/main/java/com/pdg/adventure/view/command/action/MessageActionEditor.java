package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.MessageActionData;

/**
 * Editor component for MessageActionData.
 * Allows selecting a message from the message catalog.
 */
@AutoRegisterActionEditor
public class MessageActionEditor extends AbstractMessageActionEditor<MessageActionData> {

    public MessageActionEditor(MessageActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData);
    }

    @Override
    String title() {
        return "Message Action";
    }

    @Override
    String descriptionText() {
        return "Display a message to the player from the message catalog";
    }

    @Override
    String messageId() {
        return actionData.getMessageId();
    }

    @Override
    void setMessageId(String aMessageId) {
        actionData.setMessageId(aMessageId);
    }
}
