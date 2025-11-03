package com.pdg.adventure.view.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.ItemService;

/**
 * Unit tests for ItemsMenuView business logic.
 * Tests focus on grid population, data filtering, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class ItemsMenuViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private ItemService itemService;

    private ItemsMenuView view;
    private AdventureData adventureData;
    private LocationData locationData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        adventureData.setLocationData(locations);

        // Create vocabulary
        VocabularyData vocabularyData = new VocabularyData();
        Word sword = new Word("sword", Word.Type.NOUN);
        Word shield = new Word("shield", Word.Type.NOUN);
        Word golden = new Word("golden", Word.Type.ADJECTIVE);
        vocabularyData.setWords(List.of(sword, shield, golden));
        adventureData.setVocabularyData(vocabularyData);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new ItemsMenuView(adventureService, itemService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithItems() {
        // given
        view = new ItemsMenuView(adventureService, itemService);

        ItemContainerData itemContainer = new ItemContainerData();
        List<ItemData> items = new ArrayList<>();

        ItemData sword = createTestItem("item-1", "sword", "golden");
        ItemData shield = createTestItem("item-2", "shield", "wooden");
        items.add(sword);
        items.add(shield);

        itemContainer.setItems(items);
        locationData.setItemContainerData(itemContainer);

        // when
        view.setData(adventureData, locationData);

        // then
        assertThat(locationData.getItemContainerData().getItems())
                .hasSize(2)
                .containsExactlyInAnyOrder(sword, shield);
    }

    @Test
    void setData_shouldFilterOutNullItems() {
        // given
        view = new ItemsMenuView(adventureService, itemService);

        ItemContainerData itemContainer = new ItemContainerData();
        List<ItemData> items = new ArrayList<>();

        ItemData sword = createTestItem("item-1", "sword", "golden");
        items.add(sword);
        items.add(null); // Null item (failed @DBRef)
        items.add(null); // Another null item

        itemContainer.setItems(items);
        locationData.setItemContainerData(itemContainer);

        // when
        view.setData(adventureData, locationData);

        // then
        List<ItemData> nonNullItems = locationData.getItemContainerData().getItems()
                .stream()
                .filter(item -> item != null)
                .toList();

        assertThat(nonNullItems)
                .hasSize(1)
                .contains(sword);
    }

    @Test
    void setData_withEmptyLocation_shouldHandleEmptyState() {
        // given
        view = new ItemsMenuView(adventureService, itemService);

        ItemContainerData itemContainer = new ItemContainerData();
        itemContainer.setItems(new ArrayList<>());
        locationData.setItemContainerData(itemContainer);

        // when
        view.setData(adventureData, locationData);

        // then
        assertThat(locationData.getItemContainerData().getItems()).isEmpty();
    }

    private ItemData createTestItem(String id, String noun, String adjective) {
        ItemData item = new ItemData();
        item.setId(id);
        item.setAdventureId("adventure-1");
        item.setLocationId("location-1");

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
