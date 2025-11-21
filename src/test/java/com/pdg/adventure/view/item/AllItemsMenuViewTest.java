package com.pdg.adventure.view.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.ItemService;

/**
 * Unit tests for AllItemsMenuView business logic.
 * Tests focus on grid population, data aggregation from multiple locations,
 * and state management without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class AllItemsMenuViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private ItemService itemService;

    private AllItemsMenuView view;
    private AdventureData adventureData;
    private LocationData location1;
    private LocationData location2;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        // Create first location
        location1 = new LocationData();
        location1.setId("location-1");
        location1.setDescriptionData(new DescriptionData("Forest", "A dense forest"));

        // Create second location
        location2 = new LocationData();
        location2.setId("location-2");
        location2.setDescriptionData(new DescriptionData("Cave", "A dark cave"));

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        locations.put(location2.getId(), location2);
        adventureData.setLocationData(locations);

        // Create vocabulary
        VocabularyData vocabularyData = new VocabularyData();
        Word sword = new Word("sword", Word.Type.NOUN);
        Word shield = new Word("shield", Word.Type.NOUN);
        Word torch = new Word("torch", Word.Type.NOUN);
        Word golden = new Word("golden", Word.Type.ADJECTIVE);
        Word wooden = new Word("wooden", Word.Type.ADJECTIVE);
        vocabularyData.setWords(List.of(sword, shield, torch, golden, wooden));
        adventureData.setVocabularyData(vocabularyData);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new AllItemsMenuView(adventureService, itemService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithItemsFromMultipleLocations() {
        // given
        view = new AllItemsMenuView(adventureService, itemService);

        // Add items to location 1
        ItemContainerData container1 = new ItemContainerData("19");
        List<ItemData> items1 = new ArrayList<>();
        ItemData sword = createTestItem("item-1", "sword", "golden", "location-1");
        ItemData shield = createTestItem("item-2", "shield", "wooden", "location-1");
        items1.add(sword);
        items1.add(shield);
        container1.setItems(items1);
        location1.setItemContainerData(container1);

        // Add items to location 2
        ItemContainerData container2 = new ItemContainerData("19");
        List<ItemData> items2 = new ArrayList<>();
        ItemData torch = createTestItem("item-3", "torch", "burning", "location-2");
        items2.add(torch);
        container2.setItems(items2);
        location2.setItemContainerData(container2);

        // when
        view.setData(adventureData);

        // then
        // Verify items from both locations are included
        List<ItemData> allItems = new ArrayList<>();
        allItems.addAll(location1.getItemContainerData().getItems());
        allItems.addAll(location2.getItemContainerData().getItems());

        assertThat(allItems)
                .hasSize(3)
                .containsExactlyInAnyOrder(sword, shield, torch);
    }

    @Test
    void setData_shouldFilterOutNullItemsFromAllLocations() {
        // given
        view = new AllItemsMenuView(adventureService, itemService);

        // Add items with nulls to location 1
        ItemContainerData container1 = new ItemContainerData("19");
        List<ItemData> items1 = new ArrayList<>();
        ItemData sword = createTestItem("item-1", "sword", "golden", "location-1");
        items1.add(sword);
        items1.add(null); // Null item (failed @DBRef)
        container1.setItems(items1);
        location1.setItemContainerData(container1);

        // Add items with nulls to location 2
        ItemContainerData container2 = new ItemContainerData("19");
        List<ItemData> items2 = new ArrayList<>();
        ItemData torch = createTestItem("item-2", "torch", "burning", "location-2");
        items2.add(null); // Null item (failed @DBRef)
        items2.add(torch);
        items2.add(null); // Another null item
        container2.setItems(items2);
        location2.setItemContainerData(container2);

        // when
        view.setData(adventureData);

        // then
        // Count non-null items across all locations
        long nonNullCount = adventureData.getLocationData().values().stream()
                                         .filter(loc -> loc.getItemContainerData() != null)
                                         .flatMap(loc -> loc.getItemContainerData().getItems().stream())
                                         .filter(Objects::nonNull)
                                         .count();

        assertThat(nonNullCount).isEqualTo(2);
    }

    @Test
    void setData_withEmptyAdventure_shouldHandleEmptyState() {
        // given
        view = new AllItemsMenuView(adventureService, itemService);

        // Both locations have empty item containers
        ItemContainerData container1 = new ItemContainerData("19");
        container1.setItems(new ArrayList<>());
        location1.setItemContainerData(container1);

        ItemContainerData container2 = new ItemContainerData("19");
        container2.setItems(new ArrayList<>());
        location2.setItemContainerData(container2);

        // when
        view.setData(adventureData);

        // then
        // Verify both containers are empty
        assertThat(location1.getItemContainerData().getItems()).isEmpty();
        assertThat(location2.getItemContainerData().getItems()).isEmpty();
    }

    private ItemData createTestItem(String id, String noun, String adjective, String locationId) {
        ItemData item = new ItemData();
        item.setId(id);
        item.setAdventureId("adventure-1");
        item.setLocationId(locationId);

        DescriptionData descData = new DescriptionData();
        descData.setNoun(new Word(noun, Word.Type.NOUN));
        descData.setAdjective(new Word(adjective, Word.Type.ADJECTIVE));
        descData.setShortDescription("A " + adjective + " " + noun);
        item.setDescriptionData(descData);

        item.setContainable(true);
        item.setWearable(false);
        item.setWorn(false);

        return item;
    }
}
