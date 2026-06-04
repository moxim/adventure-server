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
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class DescribeActionEditorTest {

    private AdventureData adventureData;
    private DescribeActionData describeActionData;
    private LocationData location1;
    private LocationData location2;
    private ItemData item1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create an item in location1's container
        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData itemDesc = new DescriptionData();
        itemDesc.setShortDescription("Rusty Key");
        item1.setDescriptionData(itemDesc);

        // Create location1 with an item
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

        // Create location2 for variety (no items)
        location2 = new LocationData();
        location2.setId("loc-2");
        DescriptionData locDesc2 = new DescriptionData();
        locDesc2.setShortDescription("Castle");
        location2.setDescriptionData(locDesc2);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        locations.put(location2.getId(), location2);
        adventureData.setLocationData(locations);

        describeActionData = new DescribeActionData();
    }

    @Test
    void validate_withNoTargetSelected_shouldReturnFalse() {
        // Given
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withItemTargetId_shouldReturnTrue() {
        // Given - set targetId before initialize() so buildUI() can pre-select
        describeActionData.setTargetId(item1.getId());
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validate_withLocationTargetId_shouldReturnTrue() {
        // Given - set targetId before initialize() so buildUI() can pre-select
        describeActionData.setTargetId(location1.getId());
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        // When
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(describeActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionSummary_withNoTarget_returnsNone() {
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void getActionSummary_withItemTarget_returnsCleanDescription() {
        // The combobox label is "Item: Rusty Key"; the summary uses the un-prefixed description.
        describeActionData.setTargetId(item1.getId());
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("Rusty Key");
    }

    @Test
    void getActionSummary_withLocationTarget_returnsCleanDescription() {
        describeActionData.setTargetId(location1.getId());
        DescribeActionEditor editor = new DescribeActionEditor(describeActionData, adventureData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("Forest");
    }
}
