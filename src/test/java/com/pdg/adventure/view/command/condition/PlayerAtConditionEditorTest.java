package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;

class PlayerAtConditionEditorTest {

    private AdventureData adventureData;
    private LocationData loc1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        loc1 = new LocationData();
        loc1.setId("loc-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Dark Cave");
        loc1.setDescriptionData(desc);
        loc1.setItemContainerData(new ItemContainerData("loc-1"));

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc1.getId(), loc1);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoLocationSelected_returnsFalse() {
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(new PlayerAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withLocationPreSelected_returnsTrue() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        data.setLocationId(loc1.getId());
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(new PlayerAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
