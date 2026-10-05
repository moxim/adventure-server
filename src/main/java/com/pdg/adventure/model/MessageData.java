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

    /**
     * The font this message is shown in when the adventure runs. {@link AdventureFont#DEFAULT} means "no
     * override": the message uses the adventure's own run font. Stored as the constant's name; a message
     * saved before this field existed has none and loads as {@code DEFAULT}.
     * <p>
     * TODO: Review needed — {@code DEFAULT} means "use the adventure's font", so a message cannot force the
     *  application's own font inside an adventure whose Run Font is something else.
     */
    private AdventureFont font = AdventureFont.DEFAULT;

    public MessageData(String summary, String text) {
        this.summary = summary;
        this.text = text;
    }

    public MessageData() {
    }

    /** Never null: an absent or null stored value means {@link AdventureFont#DEFAULT}. */
    public AdventureFont getFont() {
        return font == null ? AdventureFont.DEFAULT : font;
    }

    public void setFont(AdventureFont aFont) {
        font = aFont == null ? AdventureFont.DEFAULT : aFont;
    }
}
