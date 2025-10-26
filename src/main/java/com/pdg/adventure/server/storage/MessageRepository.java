package com.pdg.adventure.server.storage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pdg.adventure.model.MessageData;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for message persistence.
 * Provides CRUD operations and custom queries for MessageData.
 */
@Repository
public interface MessageRepository extends MongoRepository<MessageData, String> {

    /**
     * Find all messages for a specific adventure.
     * @param adventureId The adventure ID
     * @return List of messages belonging to the adventure
     */
    List<MessageData> findByAdventureId(String adventureId);

    /**
     * Find a specific message by adventure ID and message ID.
     * @param adventureId The adventure ID
     * @param messageId The message ID (unique within adventure)
     * @return Optional containing the message if found
     */
    Optional<MessageData> findByAdventureIdAndMessageId(String adventureId, String messageId);

    /**
     * Delete a specific message by adventure ID and message ID.
     * @param adventureId The adventure ID
     * @param messageId The message ID
     */
    void deleteByAdventureIdAndMessageId(String adventureId, String messageId);

    /**
     * Check if a message exists for an adventure.
     * @param adventureId The adventure ID
     * @param messageId The message ID
     * @return true if the message exists
     */
    boolean existsByAdventureIdAndMessageId(String adventureId, String messageId);

    /**
     * Find messages by category for an adventure.
     * @param adventureId The adventure ID
     * @param category The category name
     * @return List of messages in the category
     */
    List<MessageData> findByAdventureIdAndCategory(String adventureId, String category);

    /**
     * Delete all messages for an adventure.
     * Useful when deleting an entire adventure.
     * @param adventureId The adventure ID
     */
    void deleteByAdventureId(String adventureId);

    /**
     * Count messages for an adventure.
     * @param adventureId The adventure ID
     * @return Number of messages
     */
    long countByAdventureId(String adventureId);
}
