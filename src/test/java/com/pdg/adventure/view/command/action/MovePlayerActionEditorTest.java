package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class MovePlayerActionEditorTest {

    private AdventureData adventureData;
    private MovePlayerActionData moveActionData;
    private LocationData location1;
    private LocationData location2;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create test locations
        location1 = new LocationData();
        location1.setId("loc-1");
        DescriptionData desc1 = new DescriptionData();
        desc1.setShortDescription("Forest");
        location1.setDescriptionData(desc1);

        location2 = new LocationData();
        location2.setId("loc-2");
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Castle");
        location2.setDescriptionData(desc2);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location1.getId(), location1);
        locations.put(location2.getId(), location2);
        adventureData.setLocationData(locations);

        moveActionData = new MovePlayerActionData();
    }

    @Test
    void validate_withNoLocationSelected_shouldReturnFalse() {
        // Given
        MovePlayerActionEditor editor = new MovePlayerActionEditor(moveActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withLocationSelected_shouldReturnTrue() {
        // Given
        moveActionData.setLocationId(location1.getId());
        MovePlayerActionEditor editor = new MovePlayerActionEditor(moveActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionDataAndAdventureData() {
        // When
        MovePlayerActionEditor editor = new MovePlayerActionEditor(moveActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(moveActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        MovePlayerActionEditor editor = new MovePlayerActionEditor(moveActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionTypeName_shouldReturnMovePlayerActionName() {
        // Given
        MovePlayerActionEditor editor = new MovePlayerActionEditor(moveActionData, adventureData);

        // When
        String actionTypeName = editor.getActionTypeName();

        // Then
        assertThat(actionTypeName).isEqualTo(moveActionData.getActionName());
    }
}
