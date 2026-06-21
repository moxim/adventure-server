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
import com.pdg.adventure.model.condition.HereConditionData;

class HereConditionEditorTest {

    private AdventureData adventureData;
    private ItemData item1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        item1 = new ItemData();
        item1.setId("item-here-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Torch");
        item1.setDescriptionData(desc);
        LocationData loc = new LocationData();
        loc.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(item1);
        container.setItems(items);
        loc.setItemContainerData(container);
        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc.getId(), loc);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoItemSelected_returnsFalse() {
        HereConditionEditor editor = new HereConditionEditor(new HereConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemPreSelected_returnsTrue() {
        HereConditionData data = new HereConditionData();
        data.setThingId(item1.getId());
        HereConditionEditor editor = new HereConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        HereConditionData data = new HereConditionData();
        HereConditionEditor editor = new HereConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        HereConditionEditor editor = new HereConditionEditor(new HereConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
