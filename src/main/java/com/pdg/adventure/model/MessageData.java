package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.DatedData;

/**
 * Message data embedded in {@link AdventureData#getMessages()}. Messages are owned 1:1 by their
 * adventure - not an independent, top-level Mongo document.
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MessageData extends DatedData {
    /**
     * Unique identifier for the message within the adventure.
     * This is what actions reference (e.g., "welcome_message", "door_locked")
     */
    private String messageId;
    /**
     * The actual text content of the message.
     * This is what gets displayed to the player.
     */
    private String text;

    public MessageData(String messageId, String text) {
        this.messageId = messageId;
        this.text = text;
    }

    public MessageData() {
    }
}
