package com.pdg.adventure.server.storage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pdg.adventure.model.ItemData;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for item persistence.
 * Provides CRUD operations and custom queries for ItemData.
 */
@Repository
public interface ItemRepository extends MongoRepository<ItemData, String> {

    /**
     * Find all items for a specific adventure.
     * @param adventureId The adventure ID
     * @return List of items belonging to the adventure
     */
    List<ItemData> findByAdventureId(String adventureId);

    /**
     * Find all items in a specific location.
     * @param adventureId The adventure ID
     * @param locationId The location ID
     * @return List of items in the location
     */
    List<ItemData> findByAdventureIdAndLocationId(String adventureId, String locationId);

    /**
     * Find a specific item by ID within an adventure.
     * @param adventureId The adventure ID
     * @param id The item ID
     * @return Optional containing the item if found
     */
    Optional<ItemData> findByAdventureIdAndId(String adventureId, String id);

    /**
     * Delete a specific item.
     * @param adventureId The adventure ID
     * @param id The item ID
     */
    void deleteByAdventureIdAndId(String adventureId, String id);

    /**
     * Delete all items in a location.
     * Useful when deleting a location.
     * @param adventureId The adventure ID
     * @param locationId The location ID
     */
    void deleteByAdventureIdAndLocationId(String adventureId, String locationId);

    /**
     * Delete all items for an adventure.
     * Useful when deleting an entire adventure.
     * @param adventureId The adventure ID
     */
    void deleteByAdventureId(String adventureId);

    /**
     * Count items in a location.
     * @param adventureId The adventure ID
     * @param locationId The location ID
     * @return Number of items
     */
    long countByAdventureIdAndLocationId(String adventureId, String locationId);

    /**
     * Count items for an adventure.
     * @param adventureId The adventure ID
     * @return Number of items
     */
    long countByAdventureId(String adventureId);
}
