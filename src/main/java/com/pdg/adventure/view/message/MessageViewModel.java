package com.pdg.adventure.view.message;

import lombok.Data;

import com.pdg.adventure.model.MessageData;

/**
 * View model for message editing.
 * Adapts MessageData for use with Vaadin binders.
 */
@Data
public class MessageViewModel {
    /** The message's id; empty until a new message has been created. Never editable. */
    private String id;
    private String summary;
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
        this.summary = "";
        this.messageText = "";
        this.isNew = true;
        this.usageCount = 0;
    }

    /**
     * Constructor for editing an existing message.
     *
     * @param id          The message id
     * @param summary     The message summary
     * @param messageText The message text
     */
    public MessageViewModel(String id, String summary, String messageText) {
        this.id = id;
        this.summary = summary != null ? summary : "";
        this.messageText = messageText != null ? messageText : "";
        this.isNew = false;
        this.usageCount = 0;
    }

    /**
     * Constructor for editing an existing message with usage count.
     *
     * @param id          The message id
     * @param summary     The message summary
     * @param messageText The message text
     * @param usageCount  Number of times this message is used
     */
    public MessageViewModel(String id, String summary, String messageText, int usageCount) {
        this(id, summary, messageText);
        this.usageCount = usageCount;
    }

    /**
     * Constructor from MessageData.
     *
     * @param messageData The message data from database
     */
    public MessageViewModel(MessageData messageData) {
        this(messageData.getId(), messageData.getSummary(), messageData.getText());
    }

    /**
     * Constructor from MessageData with usage count.
     *
     * @param messageData The message data from database
     * @param usageCount  Number of times this message is used
     */
    public MessageViewModel(MessageData messageData, int usageCount) {
        this(messageData);
        this.usageCount = usageCount;
    }

    /**
     * Get a preview of the message (truncated if too long).
     *
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
        return messageText.substring(0, maxLength) + "…";
    }
}
