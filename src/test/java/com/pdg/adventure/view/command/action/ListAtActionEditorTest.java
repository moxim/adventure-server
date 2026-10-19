package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.ListAtActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class ListAtActionEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        LocationData forest = new LocationData();
        forest.setId("loc-1");
        DescriptionData description = new DescriptionData();
        description.setShortDescription("Forest");
        forest.setDescriptionData(description);
        ItemContainerData forestContainer = new ItemContainerData("loc-1");
        forestContainer.setId("container-1");
        forest.setItemContainerData(forestContainer);
        Map<String, LocationData> locations = new HashMap<>();
        locations.put(forest.getId(), forest);
        adventureData.setLocationData(locations);
        ItemContainerData pocket = new ItemContainerData("player-pocket");
        pocket.setId("pocket-1");
        adventureData.setPlayerPocket(pocket);
    }

    private ListAtActionEditor editorFor(ListAtActionData data) {
        ListAtActionEditor editor = new ListAtActionEditor(data, adventureData);
        editor.initialize();
        return editor;
    }

    @Test
    void validate_withNothingSelected_returnsFalse() {
        ListAtActionEditor editor = editorFor(new ListAtActionData());

        assertThat(editor.validate()).isFalse();
        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void aSavedLocation_isPreselected_andNamedByTheLocation() {
        ListAtActionData data = new ListAtActionData();
        data.setContainerId("container-1");
        ListAtActionEditor editor = editorFor(data);

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("Forest");
    }

    @Test
    void thePocket_isOffered_andNamedYourPocket() {
        ListAtActionData data = new ListAtActionData();
        data.setContainerId("pocket-1");
        ListAtActionEditor editor = editorFor(data);

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("your pocket");
    }
}
