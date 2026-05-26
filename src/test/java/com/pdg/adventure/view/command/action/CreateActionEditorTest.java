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
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class CreateActionEditorTest {

    private AdventureData adventureData;
    private CreateActionData createActionData;
    private LocationData location1;
    private ItemData item1;
    private ItemContainerData playerPocket;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create test item
        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc1 = new DescriptionData();
        desc1.setShortDescription("Rusty Key");
        item1.setDescriptionData(desc1);

        // Create location with item container holding the item
        location1 = new LocationData();
        location1.setId("loc-1");
        DescriptionData locDesc1 = new DescriptionData();
        locDesc1.setShortDescription("Forest");
        location1.setDescriptionData(locDesc1);

        ItemContainerData container1 = new ItemContainerData("loc-1");
        container1.setId("container-1");
        List<ItemData> items1 = new ArrayList<>();
        items1.add(item1);
        container1.setItems(items1);
        location1.setItemContainerData(container1);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        adventureData.setLocationData(locations);

        // Player pocket (empty — item is in the location)
        playerPocket = new ItemContainerData("player-pocket");
        playerPocket.setId("pocket-1");
        playerPocket.setItems(new ArrayList<>());
        adventureData.setPlayerPocket(playerPocket);

        createActionData = new CreateActionData();
    }

    @Test
    void validate_withNothingSelected_shouldReturnFalse() {
        // Given
        CreateActionEditor editor = new CreateActionEditor(createActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withOnlyItemSelected_shouldReturnFalse() {
        // Given — pre-set thingId only, no containerProviderId
        createActionData.setThingId(item1.getId());
        CreateActionEditor editor = new CreateActionEditor(createActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withOnlyContainerSelected_shouldReturnFalse() {
        // Given — pre-set containerProviderId to an existing location ID, no thingId
        createActionData.setContainerProviderId(location1.getId());
        CreateActionEditor editor = new CreateActionEditor(createActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withBothSelected_shouldReturnTrue() {
        // Given — pre-set both fields with valid IDs present in the test data
        createActionData.setThingId(item1.getId());
        createActionData.setContainerProviderId(location1.getId());
        CreateActionEditor editor = new CreateActionEditor(createActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        // When
        CreateActionEditor editor = new CreateActionEditor(createActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(createActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        CreateActionEditor editor = new CreateActionEditor(createActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
