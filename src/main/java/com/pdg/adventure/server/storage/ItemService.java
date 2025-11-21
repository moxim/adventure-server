package com.pdg.adventure.server.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pdg.adventure.model.ItemData;

/**
 * Service for managing item persistence and business logic.
 * Provides CRUD operations and querying for items.
 */
@Service
public class ItemService {
    private static final Logger LOG = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Create a new item.
     *
     * @param adventureId The adventure ID
     * @param locationId  The location ID
     * @param itemData    The item data
     * @return The created item
     */
    public ItemData createItem(@Nonnull String adventureId, @Nonnull String locationId, @Nonnull ItemData itemData) {
        LOG.info("Creating item for adventure: {} in location: {}", adventureId, locationId);

        itemData.setAdventureId(adventureId);
        itemData.setLocationId(locationId);
        if (itemData.getId() == null || itemData.getId().isEmpty()) {
            itemData.setId(UUID.randomUUID().toString());
        }
        return itemRepository.save(itemData);
    }

    /**
     * Update an existing item.
     *
     * @param itemData The item data to update
     * @return The updated item
     */
    @Transactional
    public ItemData updateItem(@Nonnull ItemData itemData) {
        LOG.info("Updating item: {} for adventure: {}", itemData.getId(), itemData.getAdventureId());

        Optional<ItemData> existingItem = itemRepository.findByAdventureIdAndId(itemData.getAdventureId(),
                                                                                itemData.getId());

        if (existingItem.isEmpty()) {
            LOG.warn("Item {} not found for adventure {}", itemData.getId(), itemData.getAdventureId());
            throw new IllegalArgumentException("Item not found");
        }

        itemData.touch(); // Update modified timestamp
        return itemRepository.save(itemData);
    }

    /**
     * Save an item (create or update).
     *
     * @param itemData The item to save
     * @return The saved item
     */
    public ItemData saveItem(@Nonnull ItemData itemData) {
        LOG.info("Saving item: {} for adventure: {}", itemData.getId(), itemData.getAdventureId());
        itemData.touch();
        return itemRepository.save(itemData);
    }

    /**
     * Delete an item.
     *
     * @param adventureId The adventure ID
     * @param itemId      The item ID
     */
    public void deleteItem(@Nonnull String adventureId, @Nonnull String itemId) {
        LOG.info("Deleting item: {} for adventure: {}", itemId, adventureId);
        itemRepository.deleteByAdventureIdAndId(adventureId, itemId);
    }

    /**
     * Get all items for an adventure.
     *
     * @param adventureId The adventure ID
     * @return List of items
     */
    public List<ItemData> getAllItemsForAdventure(@Nonnull String adventureId) {
        LOG.debug("Getting all items for adventure: {}", adventureId);
        return itemRepository.findByAdventureId(adventureId);
    }

    /**
     * Get all items in a location.
     *
     * @param adventureId The adventure ID
     * @param locationId  The location ID
     * @return List of items
     */
    public List<ItemData> getItemsForLocation(@Nonnull String adventureId, @Nonnull String locationId) {
        LOG.debug("Getting items for location: {} in adventure: {}", locationId, adventureId);
        return itemRepository.findByAdventureIdAndLocationId(adventureId, locationId);
    }

    /**
     * Get a specific item.
     *
     * @param adventureId The adventure ID
     * @param itemId      The item ID
     * @return Optional containing the item if found
     */
    public Optional<ItemData> getItemById(@Nonnull String adventureId, @Nonnull String itemId) {
        LOG.debug("Getting item: {} for adventure: {}", itemId, adventureId);
        return itemRepository.findByAdventureIdAndId(adventureId, itemId);
    }

    /**
     * Delete all items in a location.
     * Useful when deleting a location.
     *
     * @param adventureId The adventure ID
     * @param locationId  The location ID
     */
    public void deleteAllItemsForLocation(@Nonnull String adventureId, @Nonnull String locationId) {
        LOG.info("Deleting all items for location: {} in adventure: {}", locationId, adventureId);
        itemRepository.deleteByAdventureIdAndLocationId(adventureId, locationId);
    }

    /**
     * Delete all items for an adventure.
     * Useful when deleting an entire adventure.
     *
     * @param adventureId The adventure ID
     */
    public void deleteAllItemsForAdventure(@Nonnull String adventureId) {
        LOG.info("Deleting all items for adventure: {}", adventureId);
        itemRepository.deleteByAdventureId(adventureId);
    }

    /**
     * Count items in a location.
     *
     * @param adventureId The adventure ID
     * @param locationId  The location ID
     * @return Number of items
     */
    public long countItemsInLocation(@Nonnull String adventureId, @Nonnull String locationId) {
        return itemRepository.countByAdventureIdAndLocationId(adventureId, locationId);
    }

    /**
     * Count items for an adventure.
     *
     * @param adventureId The adventure ID
     * @return Number of items
     */
    public long countItemsInAdventure(@Nonnull String adventureId) {
        return itemRepository.countByAdventureId(adventureId);
    }
}
