package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.model.basic.DatedData;

/**
 * Message data stored in MongoDB.
 * Messages are stored in their own collection for better scalability and querying.
 */
@Document(collection = "messages")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@CompoundIndex(name = "adventure_message_idx", def = "{'adventureId': 1, 'messageId': 1}", unique = true)
public class MessageData extends DatedData {

    /**
     * The adventure this message belongs to.
     * Messages are scoped per adventure.
     */
    private String adventureId;

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

    /**
     * Optional category for organizing messages.
     * Examples: "greetings", "errors", "descriptions", "dialogues"
     */
    private String category;

    /**
     * Optional tags for flexible categorization.
     * Examples: "intro", "quest", "npc", "location"
     */
    private java.util.Set<String> tags;

    /**
     * Translations for internationalization support.
     * Key: language code (e.g., "en", "de", "fr")
     * Value: translated message text
     */
    private Map<String, String> translations;

    /**
     * Optional notes or comments about the message.
     * Useful for documentation and context.
     */
    private String notes;

    public MessageData() {
        this.translations = new HashMap<>();
        this.tags = new java.util.HashSet<>();
    }

    public MessageData(String adventureId, String messageId, String text) {
        this();
        this.adventureId = adventureId;
        this.messageId = messageId;
        this.text = text;
    }
}
