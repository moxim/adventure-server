package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.DatedData;

/**
 * Message data embedded in {@link AdventureData#getMessages()}, keyed by its inherited {@link #getId() id}.
 * Messages are owned 1:1 by their adventure - not an independent, top-level Mongo document. The
 * id is the only unique reference to a message (e.g. from a {@code MessageActionData}).
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MessageData extends DatedData {
    /**
     * A short, free-text summary of what the message says, shown to authors in lists and pickers.
     * Not a reference and not required to be unique.
     */
    private String summary;
    /**
     * The actual text content of the message.
     * This is what gets displayed to the player.
     */
    private String text;

    public MessageData(String summary, String text) {
        this.summary = summary;
        this.text = text;
    }

    public MessageData() {
    }
}
