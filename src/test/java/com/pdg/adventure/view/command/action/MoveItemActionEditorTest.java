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
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class MoveItemActionEditorTest {

    private AdventureData adventureData;
    private MoveItemActionData moveActionData;
    private LocationData location1;
    private LocationData location2;
    private ItemData item1;
    private ItemData item2;
    private ItemData item3;
    private ItemContainerData playerPocket;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create test items
        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc1 = new DescriptionData();
        desc1.setShortDescription("Rusty Key");
        item1.setDescriptionData(desc1);

        item2 = new ItemData();
        item2.setId("item-2");
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Golden Sword");
        item2.setDescriptionData(desc2);

        item3 = new ItemData();
        item3.setId("item-3");
        DescriptionData desc3 = new DescriptionData();
        desc3.setShortDescription("Magic Potion");
        item3.setDescriptionData(desc3);

        // Create location 1 with item container
        location1 = new LocationData();
        location1.setId("loc-1");
        DescriptionData locDesc1 = new DescriptionData();
        locDesc1.setShortDescription("Forest");
        location1.setDescriptionData(locDesc1);

        ItemContainerData container1 = new ItemContainerData("loc-1");
        container1.setId("container-1");
        List<ItemData> items1 = new ArrayList<>();
        items1.add(item1);
        items1.add(item2);
        container1.setItems(items1);
        location1.setItemContainerData(container1);

        // Create location 2 with item container
        location2 = new LocationData();
        location2.setId("loc-2");
        DescriptionData locDesc2 = new DescriptionData();
        locDesc2.setShortDescription("Castle");
        location2.setDescriptionData(locDesc2);

        ItemContainerData container2 = new ItemContainerData("loc-2");
        container2.setId("container-2");
        container2.setItems(new ArrayList<>());
        location2.setItemContainerData(container2);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        locations.put(location2.getId(), location2);
        adventureData.setLocationData(locations);

        // Create player pocket with an item
        playerPocket = new ItemContainerData("player-pocket");
        playerPocket.setId("pocket-1");
        List<ItemData> pocketItems = new ArrayList<>();
        pocketItems.add(item3);
        playerPocket.setItems(pocketItems);
        adventureData.setPlayerPocket(playerPocket);

        moveActionData = new MoveItemActionData();
    }

    @Test
    void collectAllItems_shouldIncludeItemsFromAllLocations() {
        // Given
        verifyEditorIsValid(item1, playerPocket);
    }

    @Test
    void collectAllItems_shouldIncludeItemsFromPlayerPocket() {
        verifyEditorIsValid(item3, location1.getItemContainerData());
    }

    private void verifyEditorIsValid(final ItemData item3, final ItemContainerData location1) {
        // Given
        moveActionData.setThingId(item3.getId());
        moveActionData.setDestinationId(location1.getId());
        MoveItemActionEditor editor = new MoveItemActionEditor(moveActionData, adventureData);
        editor.initialize();

        // When - Set item from player pocket
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }


    @Test
    void validate_withNoItemSelected_shouldReturnFalse() {
        // Given
        MoveItemActionEditor editor = new MoveItemActionEditor(moveActionData, adventureData);
        editor.initialize();
        moveActionData.setDestinationId(playerPocket.getId());

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withNoDestinationSelected_shouldReturnFalse() {
        // Given
        MoveItemActionEditor editor = new MoveItemActionEditor(moveActionData, adventureData);
        editor.initialize();
        moveActionData.setThingId(item1.getId());

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void constructor_shouldSetActionDataAndAdventureData() {
        // When
        MoveItemActionEditor editor = new MoveItemActionEditor(moveActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(moveActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        MoveItemActionEditor editor = new MoveItemActionEditor(moveActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionTypeName_shouldReturnMoveItemActionName() {
        // Given
        MoveItemActionEditor editor = new MoveItemActionEditor(moveActionData, adventureData);

        // When
        String actionTypeName = editor.getActionTypeName();

        // Then
        assertThat(actionTypeName).isEqualTo(moveActionData.getActionName());
    }
}
