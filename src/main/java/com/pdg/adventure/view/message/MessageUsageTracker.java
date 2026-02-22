package com.pdg.adventure.view.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking message usage throughout an adventure.
 * Scans all locations, commands, and actions to find references to specific messages.
 */
public class MessageUsageTracker {

    /**
         * Data class representing a single usage of a message.
         */
        public record MessageUsage(String locationId, String locationDescription, String commandSpecification,
                                   String actionType, String context) implements TrackedUsage {

        public String getDisplayText() {
                return "Location: %s | Command: %s | %s".formatted(
                        locationDescription != null ? locationDescription : locationId,
                        commandSpecification,
                        context);
            }
        }

    /**
     * Find all usages of a specific message in an adventure.
     *
     * @param adventureData The adventure to search
     * @param messageId     The message ID to find
     * @return List of MessageUsage objects describing where the message is used
     */
    public static List<MessageUsage> findMessageUsages(AdventureData adventureData, String messageId) {
        List<MessageUsage> usages = new ArrayList<>();

        if (adventureData == null || messageId == null || messageId.isEmpty()) {
            return usages;
        }

        // Scan through all locations
        Map<String, LocationData> locations = adventureData.getLocationData();
        if (locations != null) {
            for (Map.Entry<String, LocationData> locationEntry : locations.entrySet()) {
                LocationData location = locationEntry.getValue();
                String locationDesc = location.getDescriptionData() != null ?
                                      location.getDescriptionData().getShortDescription() : null;

                // Check commands in this location
                if (location.getCommandProviderData() != null &&
                    location.getCommandProviderData().getAvailableCommands() != null) {

                    addUsagesInLocaitonCommands(messageId, locationEntry, location, locationDesc, usages);
                }
            }
        }

        return usages;
    }

    private static void addUsagesInLocaitonCommands(final String messageId,
                                                    final Map.Entry<String, LocationData> locationEntry,
                                                    final LocationData location, final String locationDesc,
                                                    final List<MessageUsage> usages) {
        Map<String, CommandChainData> commands = location.getCommandProviderData().getAvailableCommands();

        for (Map.Entry<String, CommandChainData> commandEntry : commands.entrySet()) {
            String commandSpec = commandEntry.getKey();
            CommandChainData chain = commandEntry.getValue();

            if (chain != null && chain.getCommands() != null) {
                addUsagesInCommands(messageId, locationEntry, chain, locationDesc, commandSpec, usages);
            }
        }
    }

    private static void addUsagesInCommands(final String messageId, final Map.Entry<String, LocationData> locationEntry,
                                            final CommandChainData chain, final String locationDesc,
                                            final String commandSpec,
                                            final List<MessageUsage> usages) {
        for (CommandData command : chain.getCommands()) {
            // Check primary action
            if (command.getAction() != null) {
                checkAction(command.getAction(), locationEntry.getKey(), locationDesc,
                            commandSpec, "Primary Action", messageId, usages);
            }

            // Check follow-up actions
            if (command.getFollowUpActions() != null) {
                int followUpIndex = 1;
                for (ActionData followUpAction : command.getFollowUpActions()) {
                    checkAction(followUpAction, locationEntry.getKey(), locationDesc,
                                commandSpec, "Follow-up Action #" + followUpIndex,
                                messageId, usages);
                    followUpIndex++;
                }
            }
        }
    }

    /**
     * Check if an action uses the specified message and add to usages list if it does.
     */
    private static void checkAction(ActionData action, String locationId, String locationDesc,
                                    String commandSpec, String context, String messageId,
                                    List<MessageUsage> usages) {
        if (action instanceof MessageActionData messageAction && messageId.equals(messageAction.getMessageId())) {
            usages.add(new MessageUsage(
                    locationId,
                    locationDesc,
                    commandSpec,
                    "Message Action",
                    context
            ));
        }

    }

    /**
     * Count how many times a message is used in an adventure.
     *
     * @param adventureData The adventure to search
     * @param messageId     The message ID to count
     * @return Number of times the message is used
     */
    public static int countMessageUsages(AdventureData adventureData, String messageId) {
        return findMessageUsages(adventureData, messageId).size();
    }

    /**
     * Check if a message is used anywhere in the adventure.
     *
     * @param adventureData The adventure to search
     * @param messageId     The message ID to check
     * @return true if the message is used at least once
     */
    public static boolean isMessageUsed(AdventureData adventureData, String messageId) {
        return countMessageUsages(adventureData, messageId) > 0;
    }
}
