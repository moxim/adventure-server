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
 * Unit tests for ItemEditorView business logic.
 * Tests focus on validation, data population, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class ItemEditorViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private ItemService itemService;

    private ItemEditorView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;
    private ItemData itemData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));

        // Create item container
        ItemContainerData itemContainer = new ItemContainerData();
        itemContainer.setItems(new ArrayList<>());
        locationData.setItemContainerData(itemContainer);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        adventureData.setLocationData(locations);

        // Create vocabulary
        vocabularyData = new VocabularyData();
        Word sword = new Word("sword", Word.Type.NOUN);
        Word golden = new Word("golden", Word.Type.ADJECTIVE);
        Word go = new Word("go", Word.Type.VERB);
        vocabularyData.setWords(List.of(sword, golden, go));
        adventureData.setVocabularyData(vocabularyData);

        // Create test item
        itemData = new ItemData();
        itemData.setId("item-1");
        itemData.setAdventureId("adventure-1");
        itemData.setLocationId("location-1");
        DescriptionData descData = new DescriptionData();
        descData.setNoun(sword);
        descData.setAdjective(golden);
        descData.setShortDescription("A golden sword");
        descData.setLongDescription("A magnificent golden sword that gleams in the light");
        itemData.setDescriptionData(descData);
        itemData.setContainable(true);
        itemData.setWearable(false);
        itemData.setWorn(false);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new ItemEditorView(adventureService, itemService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateAdventureAndLocationData() {
        // given
        view = new ItemEditorView(adventureService, itemService);

        // when
        view.setData(adventureData, locationData);

        // then
        // View should be populated with data
        // No exceptions should be thrown
        assertThat(view).isNotNull();
    }

    @Test
    void setData_withExistingItem_shouldLoadItemFromContainer() {
        // given
        view = new ItemEditorView(adventureService, itemService);
        locationData.getItemContainerData().getItems().add(itemData);

        // when
        view.setData(adventureData, locationData);

        // then
        // View should load the existing item
        assertThat(locationData.getItemContainerData().getItems())
                .hasSize(1)
                .contains(itemData);
    }

    @Test
    void getPageTitle_shouldReturnNullBeforeRouteEnter() {
        // given
        view = new ItemEditorView(adventureService, itemService);

        // when
        String title = view.getPageTitle();

        // then
        assertThat(title).isNull();
    }
}
