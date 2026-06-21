package com.pdg.adventure.view.command.condition;

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
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.WornConditionData;

class WornConditionEditorTest {

    private AdventureData adventureData;
    private ItemData wearableItem;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        wearableItem = new ItemData();
        wearableItem.setId("cloak-1");
        wearableItem.setWearable(true);
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Magic Cloak");
        wearableItem.setDescriptionData(desc);

        ItemData nonWearable = new ItemData();
        nonWearable.setId("rock-1");
        nonWearable.setWearable(false);
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Rock");
        nonWearable.setDescriptionData(desc2);

        LocationData loc = new LocationData();
        loc.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(wearableItem);
        items.add(nonWearable);
        container.setItems(items);
        loc.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc.getId(), loc);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoItemSelected_returnsFalse() {
        WornConditionEditor editor = new WornConditionEditor(new WornConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withWearableItemPreSelected_returnsTrue() {
        WornConditionData data = new WornConditionData();
        data.setThingId(wearableItem.getId());
        WornConditionEditor editor = new WornConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        WornConditionData data = new WornConditionData();
        WornConditionEditor editor = new WornConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        WornConditionEditor editor = new WornConditionEditor(new WornConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
