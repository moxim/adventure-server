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
import com.pdg.adventure.model.action.LightActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class LightActionEditorTest {

    private AdventureData adventureData;
    private LightActionData lightActionData;
    private ItemData torch;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        torch = new ItemData();
        torch.setId("torch-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Torch");
        torch.setDescriptionData(desc);

        LocationData location = new LocationData();
        location.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setId("container-1");
        List<ItemData> items = new ArrayList<>();
        items.add(torch);
        container.setItems(items);
        location.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location.getId(), location);
        adventureData.setLocationData(locations);

        adventureData.setPlayerPocket(new ItemContainerData("player-pocket"));

        lightActionData = new LightActionData();
    }

    @Test
    void validate_withNoItemAndNoLumen_returnsFalse() {
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemButNoLumen_returnsFalse() {
        lightActionData.setThingId(torch.getId());
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemAndLumen_returnsTrue() {
        lightActionData.setThingId(torch.getId());
        lightActionData.setLumen(50);
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);

        assertThat(editor.getActionData()).isSameAs(lightActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);
        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionSummary_withNoSelection_returnsNone() {
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void getActionSummary_withItemAndLumenSet_includesBoth() {
        lightActionData.setThingId(torch.getId());
        lightActionData.setLumen(50);
        LightActionEditor editor = new LightActionEditor(lightActionData, adventureData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("Torch -> 50 lumen");
    }
}
