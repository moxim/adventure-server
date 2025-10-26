package com.pdg.adventure.view.message;

import lombok.Data;
import com.pdg.adventure.model.MessageData;

/**
 * View model for message editing.
 * Adapts MessageData for use with Vaadin binders.
 */
@Data
public class MessageViewModel {
    private String id;
    private String messageText;
    private boolean isNew;
    private int usageCount;
    private String category;
    private String notes;

    /**
     * Constructor for creating a new message.
     */
    public MessageViewModel() {
        this.id = "";
        this.messageText = "";
        this.isNew = true;
        this.usageCount = 0;
    }

    /**
     * Constructor for editing an existing message.
     * @param messageId The message ID
     * @param messageText The message text
     */
    public MessageViewModel(String messageId, String messageText) {
        this.id = messageId;
        this.messageText = messageText != null ? messageText : "";
        this.isNew = false;
        this.usageCount = 0;
    }

    /**
     * Constructor for editing an existing message with usage count.
     * @param messageId The message ID
     * @param messageText The message text
     * @param usageCount Number of times this message is used
     */
    public MessageViewModel(String messageId, String messageText, int usageCount) {
        this.id = messageId;
        this.messageText = messageText != null ? messageText : "";
        this.isNew = false;
        this.usageCount = usageCount;
    }

    /**
     * Constructor from MessageData.
     * @param messageData The message data from database
     */
    public MessageViewModel(MessageData messageData) {
        this.id = messageData.getMessageId();
        this.messageText = messageData.getText() != null ? messageData.getText() : "";
        this.isNew = false;
        this.usageCount = 0;
        this.category = messageData.getCategory();
        this.notes = messageData.getNotes();
    }

    /**
     * Constructor from MessageData with usage count.
     * @param messageData The message data from database
     * @param usageCount Number of times this message is used
     */
    public MessageViewModel(MessageData messageData, int usageCount) {
        this(messageData);
        this.usageCount = usageCount;
    }

    /**
     * Get a preview of the message (truncated if too long).
     * @param maxLength Maximum length of preview
     * @return Truncated message text
     */
    public String getPreview(int maxLength) {
        if (messageText == null || messageText.isEmpty()) {
            return "(empty message)";
        }
        if (messageText.length() <= maxLength) {
            return messageText;
        }
        return messageText.substring(0, maxLength) + "...";
    }

    /**
     * Check if the message ID is valid (not empty, alphanumeric with underscores).
     * @return true if valid
     */
    public boolean isValidId() {
        return id != null && !id.trim().isEmpty() && id.matches("^[a-zA-Z0-9_]+$");
    }
}
