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
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class TakeActionEditorTest {

    private AdventureData adventureData;
    private TakeActionData takeActionData;
    private ItemData item1;
    private ItemData item2;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc1 = new DescriptionData();
        desc1.setShortDescription("Golden Coin");
        item1.setDescriptionData(desc1);

        item2 = new ItemData();
        item2.setId("item-2");
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Silver Sword");
        item2.setDescriptionData(desc2);

        LocationData location1 = new LocationData();
        location1.setId("loc-1");
        DescriptionData locDesc = new DescriptionData();
        locDesc.setShortDescription("Dungeon");
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

        takeActionData = new TakeActionData();
    }

    @Test
    void validate_withNoItemSelected_shouldReturnFalse() {
        TakeActionEditor editor = new TakeActionEditor(takeActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemFromLocationPreSelected_shouldReturnTrue() {
        takeActionData.setThingId(item1.getId());
        TakeActionEditor editor = new TakeActionEditor(takeActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void validate_withItemFromPocketPreSelected_shouldReturnTrue() {
        takeActionData.setThingId(item2.getId());
        TakeActionEditor editor = new TakeActionEditor(takeActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        TakeActionEditor editor = new TakeActionEditor(takeActionData, adventureData);

        assertThat(editor.getActionData()).isSameAs(takeActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        TakeActionEditor editor = new TakeActionEditor(takeActionData, adventureData);
        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
