package com.pdg.adventure.view.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;

class ViewSupporterTest {

    private AdventureData adventureData;
    private ItemData locationItem;
    private ItemData pocketItem;
    private ItemContainerData container;
    private ItemContainerData playerPocket;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        locationItem = new ItemData();
        locationItem.setId("item-loc");

        pocketItem = new ItemData();
        pocketItem.setId("item-pocket");

        LocationData location = new LocationData();
        location.setId("loc-1");

        container = new ItemContainerData("loc-1");
        container.setId("container-1");
        List<ItemData> locItems = new ArrayList<>();
        locItems.add(locationItem);
        container.setItems(locItems);
        location.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location.getId(), location);
        adventureData.setLocationData(locations);

        playerPocket = new ItemContainerData("player-pocket");
        playerPocket.setId("pocket-1");
        List<ItemData> pocketItems = new ArrayList<>();
        pocketItems.add(pocketItem);
        playerPocket.setItems(pocketItems);
        adventureData.setPlayerPocket(playerPocket);
    }

    @Test
    void collectAllItems_shouldIncludeItemsFromLocations() {
        List<ItemData> result = ViewSupporter.collectAllItems(adventureData);
        assertThat(result).contains(locationItem);
    }

    @Test
    void collectAllItems_shouldIncludeItemsFromPlayerPocket() {
        List<ItemData> result = ViewSupporter.collectAllItems(adventureData);
        assertThat(result).contains(pocketItem);
    }

    @Test
    void collectAllItems_withNullItemList_shouldNotThrow() {
        container.setItems(null);
        List<ItemData> result = ViewSupporter.collectAllItems(adventureData);
        assertThat(result).containsExactly(pocketItem);
    }

    @Test
    void collectAllContainers_shouldReturnPocketFirst() {
        List<ItemContainerData> result = ViewSupporter.collectAllContainers(adventureData);
        assertThat(result.getFirst()).isSameAs(playerPocket);
    }

    @Test
    void collectAllContainers_shouldIncludeLocationContainers() {
        List<ItemContainerData> result = ViewSupporter.collectAllContainers(adventureData);
        assertThat(result).contains(container);
    }

    @Test
    void collectAllLocations_shouldReturnAllLocations() {
        List<LocationData> result = ViewSupporter.collectAllLocations(adventureData);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("loc-1");
    }

    @Test
    void collectAllLocations_withNoLocations_shouldReturnEmpty() {
        adventureData.setLocationData(new HashMap<>());
        List<LocationData> result = ViewSupporter.collectAllLocations(adventureData);
        assertThat(result).isEmpty();
    }
}
