package com.pdg.adventure.view.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.DescriptionData;

/**
 * Unit tests for ItemUsageTracker focusing on location-based item tracking.
 * Note: Action-based tracking tests are limited due to class hierarchy constraints
 * where item action classes (TakeActionData, DropActionData, etc.) extend BasicData
 * rather than ActionData, making it difficult to properly test in isolation.
 */
class ItemUsageTrackerTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setLocationData(new HashMap<>());
    }

    @Test
    void findItemUsages_shouldReturnEmpty_whenAdventureDataIsNull() {
        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(null, "test_item");

        // Then
        assertThat(usages).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "test_item"})
    @NullSource
    void findItemUsages_shouldReturnEmpty_whenItemIdIsNull(String argItemId) {
        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, argItemId);

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findItemUsages_shouldFindItemInLocation() {
        // Given
        String itemId = "golden_key";
        LocationData location = createLocationWithItem("loc1", "Hall", itemId);
        adventureData.getLocationData().put("loc1", location);

        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, itemId);

        // Then
        assertThat(usages).hasSize(1);
        ItemUsageTracker.ItemUsage usage = usages.get(0);
        assertThat(usage.getSourceLocationId()).isEqualTo("loc1");
        assertThat(usage.getSourceLocationDescription()).isEqualTo("Hall");
        assertThat(usage.getUsageType()).isEqualTo("Location Item");
        assertThat(usage.getContext()).isEqualTo("Item is in this location");
    }

    @Test
    void findItemUsages_shouldFindItemsInMultipleLocations() {
        // Given
        String itemId = "map";
        LocationData location1 = createLocationWithItem("loc1", "Library", itemId);
        LocationData location2 = createLocationWithItem("loc2", "Study", itemId);
        adventureData.getLocationData().put("loc1", location1);
        adventureData.getLocationData().put("loc2", location2);

        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, itemId);

        // Then
        assertThat(usages).hasSize(2);
        assertThat(usages).extracting(ItemUsageTracker.ItemUsage::getSourceLocationId)
                .containsExactlyInAnyOrder("loc1", "loc2");
    }

    @Test
    void findItemUsages_shouldNotFindDifferentItemId() {
        // Given
        LocationData location = createLocationWithItem("loc1", "Hall", "different_item");
        adventureData.getLocationData().put("loc1", location);

        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, "target_item");

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findItemUsages_shouldHandleLocationWithoutDescription() {
        // Given
        String itemId = "test_item";
        LocationData location = createLocationWithItem("loc1", null, itemId);
        adventureData.getLocationData().put("loc1", location);

        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, itemId);

        // Then
        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).getSourceLocationDescription()).isNullOrEmpty();
        assertThat(usages.get(0).getSourceLocationId()).isEqualTo("loc1");
    }

    @Test
    void findItemUsages_shouldHandleNullItemInContainer() {
        // Given
        String itemId = "valid_item";
        LocationData location = createLocationWithNullItems("loc1", "Storage", itemId);
        adventureData.getLocationData().put("loc1", location);

        // When
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, itemId);

        // Then
        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).getSourceLocationId()).isEqualTo("loc1");
    }

    @Test
    void countItemUsages_shouldReturnZero_whenNoUsages() {
        // When
        int count = ItemUsageTracker.countItemUsages(adventureData, "unused_item");

        // Then
        assertThat(count).isZero();
    }

    @Test
    void countItemUsages_shouldReturnCorrectCount() {
        // Given
        String itemId = "counted_item";
        LocationData location1 = createLocationWithItem("loc1", "Room1", itemId);
        LocationData location2 = createLocationWithItem("loc2", "Room2", itemId);
        adventureData.getLocationData().put("loc1", location1);
        adventureData.getLocationData().put("loc2", location2);

        // When
        int count = ItemUsageTracker.countItemUsages(adventureData, itemId);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void isItemUsed_shouldReturnFalse_whenNotUsed() {
        // When
        boolean isUsed = ItemUsageTracker.isItemUsed(adventureData, "unused_item");

        // Then
        assertThat(isUsed).isFalse();
    }

    @Test
    void isItemUsed_shouldReturnTrue_whenUsed() {
        // Given
        String itemId = "used_item";
        LocationData location = createLocationWithItem("loc1", "Hall", itemId);
        adventureData.getLocationData().put("loc1", location);

        // When
        boolean isUsed = ItemUsageTracker.isItemUsed(adventureData, itemId);

        // Then
        assertThat(isUsed).isTrue();
    }

    @Test
    void itemUsage_getDisplayText_shouldFormatCorrectlyForLocationItem() {
        // Given
        ItemUsageTracker.ItemUsage usage = new ItemUsageTracker.ItemUsage(
                "Location Item",
                "loc123",
                "Great Hall",
                "Item is in this location",
                null
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("Location Item");
        assertThat(displayText).contains("Great Hall");
    }

    @Test
    void itemUsage_getDisplayText_shouldFormatCorrectlyForAction() {
        // Given
        ItemUsageTracker.ItemUsage usage = new ItemUsageTracker.ItemUsage(
                "Take Action",
                "loc123",
                "Armory",
                "Primary Action",
                "take sword"
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("Take Action");
        assertThat(displayText).contains("Armory");
        assertThat(displayText).contains("take sword");
        assertThat(displayText).contains("Primary Action");
    }

    @Test
    void itemUsage_getDisplayText_shouldUseLocationId_whenDescriptionIsNull() {
        // Given
        ItemUsageTracker.ItemUsage usage = new ItemUsageTracker.ItemUsage(
                "Location Item",
                "loc123",
                null,
                "Item is in this location",
                null
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("loc123");
    }

    // Helper methods

    private LocationData createLocationWithItem(String locationId, String description, String itemId) {
        LocationData location = new LocationData();
        location.setId(locationId);

        if (description != null) {
            DescriptionData descData = new DescriptionData();
            descData.setShortDescription(description);
            location.setDescriptionData(descData);
        }

        // Add item to location's ItemContainer
        ItemContainerData itemContainer = new ItemContainerData("19");
        List<ItemData> items = new ArrayList<>();
        ItemData item = new ItemData();
        item.setId(itemId);
        items.add(item);
        itemContainer.setItems(items);
        location.setItemContainerData(itemContainer);

        return location;
    }

    private LocationData createLocationWithNullItems(String locationId, String description, String itemId) {
        LocationData location = new LocationData();
        location.setId(locationId);

        DescriptionData descData = new DescriptionData();
        descData.setShortDescription(description);
        location.setDescriptionData(descData);

        // Add item to location's ItemContainer with null items mixed in
        ItemContainerData itemContainer = new ItemContainerData("19");
        List<ItemData> items = new ArrayList<>();
        items.add(null); // Null item (failed @DBRef)
        ItemData item = new ItemData();
        item.setId(itemId);
        items.add(item);
        items.add(null); // Another null item
        itemContainer.setItems(items);
        location.setItemContainerData(itemContainer);

        return location;
    }
}
