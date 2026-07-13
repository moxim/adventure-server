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
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class DropActionEditorTest {

    private AdventureData adventureData;
    private DropActionData dropActionData;
    private ItemData item1;
    private ItemData item2;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc1 = new DescriptionData();
        desc1.setShortDescription("Wooden Stick");
        item1.setDescriptionData(desc1);

        item2 = new ItemData();
        item2.setId("item-2");
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Gold Coin");
        item2.setDescriptionData(desc2);

        LocationData location1 = new LocationData();
        location1.setId("loc-1");
        DescriptionData locDesc = new DescriptionData();
        locDesc.setShortDescription("Cave");
        location1.setDescriptionData(locDesc);

        ItemContainerData container = new ItemContainerData("loc-1");
        container.setId("container-1");
        List<ItemData> items = new ArrayList<>();
        items.add(item1);
        container.setItems(items);
        location1.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        adventureData.setLocationData(locations);

        ItemContainerData pocket = new ItemContainerData("player-pocket");
        pocket.setId("pocket-1");
        List<ItemData> pocketItems = new ArrayList<>();
        pocketItems.add(item2);
        pocket.setItems(pocketItems);
        adventureData.setPlayerPocket(pocket);

        dropActionData = new DropActionData();
    }

    @Test
    void validate_withNoItemSelected_shouldReturnFalse() {
        DropActionEditor editor = new DropActionEditor(dropActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemFromLocationPreSelected_shouldReturnTrue() {
        dropActionData.setThingId(item1.getId());
        DropActionEditor editor = new DropActionEditor(dropActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void validate_withItemFromPocketPreSelected_shouldReturnTrue() {
        dropActionData.setThingId(item2.getId());
        DropActionEditor editor = new DropActionEditor(dropActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        DropActionEditor editor = new DropActionEditor(dropActionData, adventureData);

        assertThat(editor.getActionData()).isSameAs(dropActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        DropActionEditor editor = new DropActionEditor(dropActionData, adventureData);
        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
