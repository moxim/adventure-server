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
import com.pdg.adventure.model.condition.CarriedConditionData;

class CarriedConditionEditorTest {

    private AdventureData adventureData;
    private ItemData item1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Golden Sword");
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
        CarriedConditionData data = new CarriedConditionData();
        CarriedConditionEditor editor = new CarriedConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemPreSelected_returnsTrue() {
        CarriedConditionData data = new CarriedConditionData();
        data.setItemId(item1.getId());
        CarriedConditionEditor editor = new CarriedConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        CarriedConditionData data = new CarriedConditionData();
        CarriedConditionEditor editor = new CarriedConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        CarriedConditionEditor editor = new CarriedConditionEditor(new CarriedConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withNoSelection_returnsNone() {
        CarriedConditionEditor editor = new CarriedConditionEditor(new CarriedConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }
}
