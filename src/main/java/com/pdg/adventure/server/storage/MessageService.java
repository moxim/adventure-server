package com.pdg.adventure.server.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pdg.adventure.model.MessageData;

/**
 * Service for managing message persistence and business logic.
 * Provides CRUD operations and querying for messages.
 */
@Service
public class MessageService {
    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Create a new message.
     * @param adventureId The adventure ID
     * @param messageId The message ID (unique within adventure)
     * @param text The message text
     * @return The created message
     */
    public MessageData createMessage(@Nonnull String adventureId, @Nonnull String messageId, @Nonnull String text) {
        LOG.info("Creating message: {} for adventure: {}", messageId, adventureId);

        // Check if message already exists
        if (messageRepository.existsByAdventureIdAndMessageId(adventureId, messageId)) {
            LOG.warn("Message {} already exists for adventure {}", messageId, adventureId);
            throw new IllegalArgumentException("Message with ID '" + messageId + "' already exists for this adventure");
        }

        MessageData message = new MessageData(adventureId, messageId, text);
        message.setId(UUID.randomUUID().toString());
        return messageRepository.save(message);
    }

    /**
     * Update an existing message.
     * If changing the messageId, this will create a new entry and delete the old one.
     * @param adventureId The adventure ID
     * @param oldMessageId The current message ID
     * @param newMessageId The new message ID (can be same as old)
     * @param newText The new message text
     * @return The updated message
     */
    @Transactional
    public MessageData updateMessage(@Nonnull String adventureId, @Nonnull String oldMessageId,
                                    @Nonnull String newMessageId, @Nonnull String newText) {
        LOG.info("Updating message: {} -> {} for adventure: {}", oldMessageId, newMessageId, adventureId);

        Optional<MessageData> existingMessage = messageRepository.findByAdventureIdAndMessageId(adventureId, oldMessageId);

        if (existingMessage.isEmpty()) {
            LOG.warn("Message {} not found for adventure {}", oldMessageId, adventureId);
            throw new IllegalArgumentException("Message not found");
        }

        MessageData message = existingMessage.get();

        // If messageId is changing, check if new ID already exists
        if (!oldMessageId.equals(newMessageId)) {
            if (messageRepository.existsByAdventureIdAndMessageId(adventureId, newMessageId)) {
                LOG.warn("Cannot rename: Message {} already exists for adventure {}", newMessageId, adventureId);
                throw new IllegalArgumentException("A message with ID '" + newMessageId + "' already exists");
            }
            message.setMessageId(newMessageId);
        }

        message.setText(newText);
        message.touch(); // Update modified timestamp
        return messageRepository.save(message);
    }

    /**
     * Delete a message.
     * @param adventureId The adventure ID
     * @param messageId The message ID
     */
    public void deleteMessage(@Nonnull String adventureId, @Nonnull String messageId) {
        LOG.info("Deleting message: {} for adventure: {}", messageId, adventureId);
        messageRepository.deleteByAdventureIdAndMessageId(adventureId, messageId);
    }

    /**
     * Get all messages for an adventure.
     * @param adventureId The adventure ID
     * @return List of messages
     */
    public List<MessageData> getAllMessagesForAdventure(@Nonnull String adventureId) {
        LOG.debug("Getting all messages for adventure: {}", adventureId);
        return messageRepository.findByAdventureId(adventureId);
    }

    /**
     * Get a specific message.
     * @param adventureId The adventure ID
     * @param messageId The message ID
     * @return Optional containing the message if found
     */
    public Optional<MessageData> getMessageByIdForAdventure(@Nonnull String adventureId, @Nonnull String messageId) {
        LOG.debug("Getting message: {} for adventure: {}", messageId, adventureId);
        return messageRepository.findByAdventureIdAndMessageId(adventureId, messageId);
    }

    /**
     * Get the text of a message.
     * @param adventureId The adventure ID
     * @param messageId The message ID
     * @return The message text, or null if not found
     */
    public String getMessageText(@Nonnull String adventureId, @Nonnull String messageId) {
        return getMessageByIdForAdventure(adventureId, messageId)
                .map(MessageData::getText)
                .orElse(null);
    }

    /**
     * Check if a message exists.
     * @param adventureId The adventure ID
     * @param messageId The message ID
     * @return true if the message exists
     */
    public boolean messageExists(@Nonnull String adventureId, @Nonnull String messageId) {
        return messageRepository.existsByAdventureIdAndMessageId(adventureId, messageId);
    }

    /**
     * Get messages by category.
     * @param adventureId The adventure ID
     * @param category The category name
     * @return List of messages in the category
     */
    public List<MessageData> getMessagesByCategory(@Nonnull String adventureId, @Nonnull String category) {
        LOG.debug("Getting messages by category: {} for adventure: {}", category, adventureId);
        return messageRepository.findByAdventureIdAndCategory(adventureId, category);
    }

    /**
     * Delete all messages for an adventure.
     * Useful when deleting an entire adventure.
     * @param adventureId The adventure ID
     */
    public void deleteAllMessagesForAdventure(@Nonnull String adventureId) {
        LOG.info("Deleting all messages for adventure: {}", adventureId);
        messageRepository.deleteByAdventureId(adventureId);
    }

    /**
     * Count messages for an adventure.
     * @param adventureId The adventure ID
     * @return Number of messages
     */
    public long countMessages(@Nonnull String adventureId) {
        return messageRepository.countByAdventureId(adventureId);
    }

    /**
     * Save a message (create or update).
     * @param message The message to save
     * @return The saved message
     */
    public MessageData saveMessage(@Nonnull MessageData message) {
        LOG.info("Saving message: {} for adventure: {}", message.getMessageId(), message.getAdventureId());
        message.touch();
        return messageRepository.save(message);
    }
}
