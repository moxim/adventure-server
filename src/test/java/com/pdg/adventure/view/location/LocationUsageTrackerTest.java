package com.pdg.adventure.view.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class LocationUsageTrackerTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setLocationData(new HashMap<>());
    }

    @Test
    void findLocationUsages_shouldReturnEmpty_whenAdventureDataIsNull() {
        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(null, "test_loc");

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findLocationUsages_shouldReturnEmpty_whenLocationIdIsNull() {
        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData, null);

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findLocationUsages_shouldReturnEmpty_whenLocationIdIsEmpty() {
        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData, "");

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findLocationUsages_shouldReturnEmpty_whenNoLocations() {
        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  "test_loc");

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findLocationUsages_shouldFindStartingLocation() {
        // Given
        String startingLocationId = "entrance";
        adventureData.setCurrentLocationId(startingLocationId);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  startingLocationId);

        // Then
        assertThat(usages).hasSize(1);
        LocationUsageTracker.LocationUsage usage = usages.getFirst();
        assertThat(usage.getUsageType()).isEqualTo("Starting Location");
        assertThat(usage.getContext()).contains("starting location");
    }

    @Test
    void findLocationUsages_shouldFindDirectionUsage() {
        // Given
        String targetLocationId = "library";
        LocationData hallLocation = createLocationWithDirection("hall", "Great Hall", targetLocationId, "north");
        adventureData.getLocationData().put("hall", hallLocation);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  targetLocationId);

        // Then
        assertThat(usages).hasSize(1);
        LocationUsageTracker.LocationUsage usage = usages.getFirst();
        assertThat(usage.getUsageType()).isEqualTo("Direction");
        assertThat(usage.getSourceLocationId()).isEqualTo("hall");
        assertThat(usage.getSourceLocationDescription()).isEqualTo("Great Hall");
        assertThat(usage.getContext()).contains("north");
    }

    @Test
    void findLocationUsages_shouldFindMultipleDirections() {
        // Given
        String targetLocationId = "courtyard";
        LocationData hallLocation = createLocationWithDirection("hall", "Hall", targetLocationId, "north");
        LocationData kitchenLocation = createLocationWithDirection("kitchen", "Kitchen", targetLocationId, "east");
        adventureData.getLocationData().put("hall", hallLocation);
        adventureData.getLocationData().put("kitchen", kitchenLocation);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  targetLocationId);

        // Then
        assertThat(usages).hasSize(2);
        assertThat(usages).extracting(LocationUsageTracker.LocationUsage::getUsageType)
                          .containsOnly("Direction");
        assertThat(usages).extracting(LocationUsageTracker.LocationUsage::getSourceLocationId)
                          .containsExactlyInAnyOrder("hall", "kitchen");
    }

    @Test
    void findLocationUsages_shouldFindMovePlayerAction() {
        // Given
        String targetLocationId = "dungeon";
        LocationData hallLocation = createLocationWithMoveAction("hall", "Hall", "use trap door", targetLocationId);
        adventureData.getLocationData().put("hall", hallLocation);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  targetLocationId);

        // Then
        assertThat(usages).hasSize(1);
        LocationUsageTracker.LocationUsage usage = usages.getFirst();
        assertThat(usage.getUsageType()).isEqualTo("Move Action");
        assertThat(usage.getSourceLocationId()).isEqualTo("hall");
        assertThat(usage.getSourceLocationDescription()).isEqualTo("Hall");
        assertThat(usage.getCommandSpecification()).isEqualTo("use trap door");
        assertThat(usage.getContext()).isEqualTo("Primary Action");
    }

    @Test
    void findLocationUsages_shouldFindMoveActionInFollowUp() {
        // Given
        String targetLocationId = "treasure_room";
        LocationData hallLocation = createLocationWithMoveActionInFollowUp("hall", "Hall", "solve puzzle",
                                                                           targetLocationId);
        adventureData.getLocationData().put("hall", hallLocation);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  targetLocationId);

        // Then
        assertThat(usages).hasSize(1);
        LocationUsageTracker.LocationUsage usage = usages.getFirst();
        assertThat(usage.getUsageType()).isEqualTo("Move Action");
        assertThat(usage.getContext()).isEqualTo("Follow-up Action #1");
    }

    @Test
    void findLocationUsages_shouldFindMultipleUsageTypes() {
        // Given
        String targetLocationId = "garden";
        adventureData.setCurrentLocationId(targetLocationId); // Starting location

        LocationData hallLocation = createLocationWithDirection("hall", "Hall", targetLocationId, "west");
        adventureData.getLocationData().put("hall", hallLocation);

        LocationData kitchenLocation = createLocationWithMoveAction("kitchen", "Kitchen", "open window",
                                                                    targetLocationId);
        adventureData.getLocationData().put("kitchen", kitchenLocation);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  targetLocationId);

        // Then
        assertThat(usages).hasSize(3);
        assertThat(usages).extracting(LocationUsageTracker.LocationUsage::getUsageType)
                          .containsExactlyInAnyOrder("Starting Location", "Direction", "Move Action");
    }

    @Test
    void findLocationUsages_shouldNotFindDifferentLocationId() {
        // Given
        LocationData location = createLocationWithDirection("hall", "Hall", "library", "north");
        adventureData.getLocationData().put("hall", location);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  "dungeon");

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findLocationUsages_shouldHandleLocationWithoutDescription() {
        // Given
        String targetLocationId = "tower";
        LocationData location = createLocationWithDirection("loc1", null, targetLocationId, "up");
        adventureData.getLocationData().put("loc1", location);

        // When
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  targetLocationId);

        // Then
        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().getSourceLocationDescription()).isNullOrEmpty();
        assertThat(usages.getFirst().getSourceLocationId()).isEqualTo("loc1");
    }

    @Test
    void countLocationUsages_shouldReturnZero_whenNoUsages() {
        // When
        int count = LocationUsageTracker.countLocationUsages(adventureData, "unused_loc");

        // Then
        assertThat(count).isZero();
    }

    @Test
    void countLocationUsages_shouldReturnCorrectCount() {
        // Given
        String targetLocationId = "library";
        adventureData.setCurrentLocationId(targetLocationId);
        LocationData hall = createLocationWithDirection("hall", "Hall", targetLocationId, "north");
        LocationData kitchen = createLocationWithDirection("kitchen", "Kitchen", targetLocationId, "east");
        adventureData.getLocationData().put("hall", hall);
        adventureData.getLocationData().put("kitchen", kitchen);

        // When
        int count = LocationUsageTracker.countLocationUsages(adventureData, targetLocationId);

        // Then
        assertThat(count).isEqualTo(3); // Starting + 2 directions
    }

    @Test
    void isLocationUsed_shouldReturnFalse_whenNotUsed() {
        // When
        boolean isUsed = LocationUsageTracker.isLocationUsed(adventureData, "unused_loc");

        // Then
        assertThat(isUsed).isFalse();
    }

    @Test
    void isLocationUsed_shouldReturnTrue_whenUsed() {
        // Given
        String targetLocationId = "used_loc";
        adventureData.setCurrentLocationId(targetLocationId);

        // When
        boolean isUsed = LocationUsageTracker.isLocationUsed(adventureData, targetLocationId);

        // Then
        assertThat(isUsed).isTrue();
    }

    @Test
    void locationUsage_getDisplayText_shouldFormatDirectionCorrectly() {
        // Given
        LocationUsageTracker.LocationUsage usage = new LocationUsageTracker.LocationUsage(
                "Direction",
                "hall_id",
                "Great Hall",
                "Direction 'north'",
                null
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("Direction:");
        assertThat(displayText).contains("Great Hall");
        assertThat(displayText).contains("north");
        assertThat(displayText).contains("→");
    }

    @Test
    void locationUsage_getDisplayText_shouldFormatMoveActionCorrectly() {
        // Given
        LocationUsageTracker.LocationUsage usage = new LocationUsageTracker.LocationUsage(
                "Move Action",
                "hall_id",
                "Great Hall",
                "Primary Action",
                "use door"
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("Move Action:");
        assertThat(displayText).contains("Great Hall");
        assertThat(displayText).contains("use door");
        assertThat(displayText).contains("Primary Action");
    }

    @Test
    void locationUsage_getDisplayText_shouldFormatStartingLocationCorrectly() {
        // Given
        LocationUsageTracker.LocationUsage usage = new LocationUsageTracker.LocationUsage(
                "Starting Location",
                null,
                null,
                "This is the starting location",
                null
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("Starting Location:");
        assertThat(displayText).contains("starting location");
    }

    @Test
    void locationUsage_getDisplayText_shouldUseLocationId_whenDescriptionIsNull() {
        // Given
        LocationUsageTracker.LocationUsage usage = new LocationUsageTracker.LocationUsage(
                "Direction",
                "loc123",
                null,
                "Direction 'south'",
                null
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("loc123");
    }

    // Helper methods

    private LocationData createLocationWithDirection(String locationId, String description,
                                                     String destinationId, String directionName) {
        LocationData location = new LocationData();
        location.setId(locationId);

        if (description != null) {
            DescriptionData descData = new DescriptionData();
            descData.setShortDescription(description);
            location.setDescriptionData(descData);
        }

        DirectionData direction = new DirectionData();
        direction.setDestinationId(destinationId);
        DescriptionData directionDesc = new DescriptionData();
        directionDesc.setShortDescription(directionName);
        direction.setDescriptionData(directionDesc);

        Set<DirectionData> directions = new HashSet<>();
        directions.add(direction);
        location.setDirectionsData(directions);

        return location;
    }

    private LocationData createLocationWithMoveAction(String locationId, String description,
                                                      String commandSpec, String targetLocationId) {
        LocationData location = new LocationData();
        location.setId(locationId);

        if (description != null) {
            DescriptionData descData = new DescriptionData();
            descData.setShortDescription(description);
            location.setDescriptionData(descData);
        }

        CommandProviderData commandProvider = new CommandProviderData();
        Map<String, CommandChainData> commands = new HashMap<>();

        CommandChainData commandChain = new CommandChainData();
        CommandData command = new CommandData();

        MovePlayerActionData moveAction = new MovePlayerActionData();
        moveAction.setLocationId(targetLocationId);
        command.setAction(moveAction);

        commandChain.getCommands().add(command);
        commands.put(commandSpec, commandChain);
        commandProvider.setAvailableCommands(commands);

        location.setCommandProviderData(commandProvider);
        return location;
    }

    private LocationData createLocationWithMoveActionInFollowUp(String locationId, String description,
                                                                String commandSpec, String targetLocationId) {
        LocationData location = new LocationData();
        location.setId(locationId);

        if (description != null) {
            DescriptionData descData = new DescriptionData();
            descData.setShortDescription(description);
            location.setDescriptionData(descData);
        }

        CommandProviderData commandProvider = new CommandProviderData();
        Map<String, CommandChainData> commands = new HashMap<>();

        CommandChainData commandChain = new CommandChainData();
        CommandData command = new CommandData();

        // Primary action is not a move action
        command.setAction(new MovePlayerActionData()); // dummy action

        // Follow-up action is a move action
        MovePlayerActionData followUpMove = new MovePlayerActionData();
        followUpMove.setLocationId(targetLocationId);

        List<MovePlayerActionData> followUpActions = new ArrayList<>();
        followUpActions.add(followUpMove);
        command.setFollowUpActions(followUpActions);

        commandChain.getCommands().add(command);
        commands.put(commandSpec, commandChain);
        commandProvider.setAvailableCommands(commands);

        location.setCommandProviderData(commandProvider);
        return location;
    }
}
