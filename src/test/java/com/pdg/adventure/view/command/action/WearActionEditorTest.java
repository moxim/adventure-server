package com.pdg.adventure.view.command.action;

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
import com.pdg.adventure.model.action.WearActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class WearActionEditorTest {

    private AdventureData adventureData;
    private WearActionData wearActionData;
    private ItemData item1;
    private ItemData item2;
    private ItemData item3;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create test items
        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc1 = new DescriptionData();
        desc1.setShortDescription("Iron Helmet");
        item1.setDescriptionData(desc1);

        item2 = new ItemData();
        item2.setId("item-2");
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Magic Robe");
        item2.setDescriptionData(desc2);

        item3 = new ItemData();
        item3.setId("item-3");
        DescriptionData desc3 = new DescriptionData();
        desc3.setShortDescription("Leather Boots");
        item3.setDescriptionData(desc3);

        // Create location with item container
        LocationData location1 = new LocationData();
        location1.setId("loc-1");
        DescriptionData locDesc1 = new DescriptionData();
        locDesc1.setShortDescription("Armory");
        location1.setDescriptionData(locDesc1);

        ItemContainerData container1 = new ItemContainerData("loc-1");
        container1.setId("container-1");
        List<ItemData> items1 = new ArrayList<>();
        items1.add(item1);
        items1.add(item2);
        container1.setItems(items1);
        location1.setItemContainerData(container1);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        adventureData.setLocationData(locations);

        // Create player pocket with an item
        ItemContainerData playerPocket = new ItemContainerData("player-pocket");
        playerPocket.setId("pocket-1");
        List<ItemData> pocketItems = new ArrayList<>();
        pocketItems.add(item3);
        playerPocket.setItems(pocketItems);
        adventureData.setPlayerPocket(playerPocket);

        wearActionData = new WearActionData();
    }

    @Test
    void validate_withNoItemSelected_shouldReturnFalse() {
        // Given
        WearActionEditor editor = new WearActionEditor(wearActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withItemPreSelected_shouldReturnTrue() {
        // Given - pre-populate thingId BEFORE initialize() to test pre-selection
        wearActionData.setThingId(item1.getId());
        WearActionEditor editor = new WearActionEditor(wearActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validate_withItemFromPlayerPocketPreSelected_shouldReturnTrue() {
        // Given - item from player's pocket
        wearActionData.setThingId(item3.getId());
        WearActionEditor editor = new WearActionEditor(wearActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        // When
        WearActionEditor editor = new WearActionEditor(wearActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(wearActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        WearActionEditor editor = new WearActionEditor(wearActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionTypeName_shouldReturnWearActionName() {
        // Given
        WearActionEditor editor = new WearActionEditor(wearActionData, adventureData);

        // When
        String actionTypeName = editor.getActionTypeName();

        // Then
        assertThat(actionTypeName).isEqualTo(wearActionData.getActionName());
    }
}
