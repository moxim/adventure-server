package com.pdg.adventure.view.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.basic.DescriptionData;

class MessageUsageTrackerTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setLocationData(new HashMap<>());
    }

    @Test
    void findMessageUsages_shouldReturnEmpty_whenAdventureDataIsNull() {
        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(null, "test_msg");

        // Then
        assertThat(usages).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "test_msg"})
    @NullSource
    void findMessageUsages_shouldReturnEmpty_whenMessageIdIsNull(String argMessageId) {
        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData,
                                                                                              argMessageId);

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findMessageUsages_shouldFindUsageInPrimaryAction() {
        // Given
        String messageId = "welcome_msg";
        LocationData location = createLocationWithMessage("loc1", "Hall", "get key", messageId);
        adventureData.getLocationData().put("loc1", location);

        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData, messageId);

        // Then
        assertThat(usages).hasSize(1);
        MessageUsageTracker.MessageUsage usage = usages.getFirst();
        assertThat(usage.locationId()).isEqualTo("loc1");
        assertThat(usage.locationDescription()).isEqualTo("Hall");
        assertThat(usage.commandSpecification()).isEqualTo("get key");
        assertThat(usage.actionType()).isEqualTo("Message Action");
        assertThat(usage.context()).isEqualTo("Primary Action");
    }

    @Test
    void findMessageUsages_shouldFindMultipleUsagesInSameLocation() {
        // Given
        String messageId = "door_msg";
        LocationData location = createLocationWithMultipleMessages("loc1", "Room", messageId);
        adventureData.getLocationData().put("loc1", location);

        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData, messageId);

        // Then
        assertThat(usages).hasSize(3); // Primary + 2 follow-ups
    }

    @Test
    void findMessageUsages_shouldFindUsagesAcrossMultipleLocations() {
        // Given
        String messageId = "common_msg";
        LocationData location1 = createLocationWithMessage("loc1", "Hall", "open door", messageId);
        LocationData location2 = createLocationWithMessage("loc2", "Kitchen", "use key", messageId);
        adventureData.getLocationData().put("loc1", location1);
        adventureData.getLocationData().put("loc2", location2);

        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData, messageId);

        // Then
        assertThat(usages).hasSize(2);
        assertThat(usages).extracting(MessageUsageTracker.MessageUsage::locationId)
                          .containsExactlyInAnyOrder("loc1", "loc2");
    }

    @Test
    void findMessageUsages_shouldNotFindDifferentMessageId() {
        // Given
        LocationData location = createLocationWithMessage("loc1", "Hall", "get key", "different_msg");
        adventureData.getLocationData().put("loc1", location);

        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData,
                                                                                              "target_msg");

        // Then
        assertThat(usages).isEmpty();
    }

    @Test
    void findMessageUsages_shouldHandleLocationWithoutDescription() {
        // Given
        String messageId = "test_msg";
        LocationData location = createLocationWithMessage("loc1", null, "look around", messageId);
        adventureData.getLocationData().put("loc1", location);

        // When
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData, messageId);

        // Then
        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().locationDescription()).isNullOrEmpty();
        assertThat(usages.getFirst().locationId()).isEqualTo("loc1");
        assertThat(usages.getFirst().commandSpecification()).isEqualTo("look around");
    }

    @Test
    void countMessageUsages_shouldReturnZero_whenNoUsages() {
        // When
        int count = MessageUsageTracker.countMessageUsages(adventureData, "unused_msg");

        // Then
        assertThat(count).isZero();
    }

    @Test
    void countMessageUsages_shouldReturnCorrectCount() {
        // Given
        String messageId = "counted_msg";
        LocationData location = createLocationWithMultipleMessages("loc1", "Room", messageId);
        adventureData.getLocationData().put("loc1", location);

        // When
        int count = MessageUsageTracker.countMessageUsages(adventureData, messageId);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void isMessageUsed_shouldReturnFalse_whenNotUsed() {
        // When
        boolean isUsed = MessageUsageTracker.isMessageUsed(adventureData, "unused_msg");

        // Then
        assertThat(isUsed).isFalse();
    }

    @Test
    void isMessageUsed_shouldReturnTrue_whenUsed() {
        // Given
        String messageId = "used_msg";
        LocationData location = createLocationWithMessage("loc1", "Hall", "test", messageId);
        adventureData.getLocationData().put("loc1", location);

        // When
        boolean isUsed = MessageUsageTracker.isMessageUsed(adventureData, messageId);

        // Then
        assertThat(isUsed).isTrue();
    }

    @Test
    void messageUsage_getDisplayText_shouldFormatCorrectly() {
        // Given
        MessageUsageTracker.MessageUsage usage = new MessageUsageTracker.MessageUsage(
                "loc123",
                "Great Hall",
                "open door",
                "Message Action",
                "Primary Action"
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("Great Hall");
        assertThat(displayText).contains("open door");
        assertThat(displayText).contains("Primary Action");
    }

    @Test
    void messageUsage_getDisplayText_shouldUseLocationId_whenDescriptionIsNull() {
        // Given
        MessageUsageTracker.MessageUsage usage = new MessageUsageTracker.MessageUsage(
                "loc123",
                null,
                "open door",
                "Message Action",
                "Primary Action"
        );

        // When
        String displayText = usage.getDisplayText();

        // Then
        assertThat(displayText).contains("loc123");
        assertThat(displayText).contains("open door");
    }

    // Helper methods

    private LocationData createLocationWithMessage(String locationId, String description,
                                                   String commandSpec, String messageId) {
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

        MessageActionData messageAction = new MessageActionData();
        messageAction.setMessageId(messageId);
        command.setAction(messageAction);

        commandChain.getCommands().add(command);
        commands.put(commandSpec, commandChain);
        commandProvider.setAvailableCommands(commands);

        location.setCommandProviderData(commandProvider);
        return location;
    }

    private LocationData createLocationWithMultipleMessages(String locationId, String description, String messageId) {
        LocationData location = new LocationData();
        location.setId(locationId);

        DescriptionData descData = new DescriptionData();
        descData.setShortDescription(description);
        location.setDescriptionData(descData);

        CommandProviderData commandProvider = new CommandProviderData();
        Map<String, CommandChainData> commands = new HashMap<>();

        CommandChainData commandChain = new CommandChainData();
        CommandData command = new CommandData();

        // Primary action
        MessageActionData primaryAction = new MessageActionData();
        primaryAction.setMessageId(messageId);
        command.setAction(primaryAction);

        // Follow-up actions
        MessageActionData followUp1 = new MessageActionData();
        followUp1.setMessageId(messageId);
        MessageActionData followUp2 = new MessageActionData();
        followUp2.setMessageId(messageId);

        List<MessageActionData> followUpActions = new java.util.ArrayList<>();
        followUpActions.add(followUp1);
        followUpActions.add(followUp2);
        command.setFollowUpActions(followUpActions);

        commandChain.getCommands().add(command);
        commands.put("test_command", commandChain);
        commandProvider.setAvailableCommands(commands);

        location.setCommandProviderData(commandProvider);
        return location;
    }
}
