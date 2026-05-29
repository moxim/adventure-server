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
import com.pdg.adventure.model.condition.ItemAtConditionData;

class ItemAtConditionEditorTest {

    private AdventureData adventureData;
    private ItemData item1;
    private LocationData loc1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData iDesc = new DescriptionData();
        iDesc.setShortDescription("Key");
        item1.setDescriptionData(iDesc);

        loc1 = new LocationData();
        loc1.setId("loc-1");
        DescriptionData lDesc = new DescriptionData();
        lDesc.setShortDescription("Dungeon");
        loc1.setDescriptionData(lDesc);

        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(item1);
        container.setItems(items);
        loc1.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc1.getId(), loc1);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNothingSelected_returnsFalse() {
        ItemAtConditionEditor editor = new ItemAtConditionEditor(new ItemAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withBothPreSelected_returnsTrue() {
        ItemAtConditionData data = new ItemAtConditionData();
        data.setThingId(item1.getId());
        data.setLocationId(loc1.getId());
        ItemAtConditionEditor editor = new ItemAtConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        ItemAtConditionData data = new ItemAtConditionData();
        ItemAtConditionEditor editor = new ItemAtConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        ItemAtConditionEditor editor = new ItemAtConditionEditor(new ItemAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
