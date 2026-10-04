package com.pdg.adventure.view.message;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.view.support.ActionScanner;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking message usage throughout an adventure.
 * Scans every place a command can hold actions (see {@link ActionScanner}) to find references to a message.
 */
public class MessageUsageTracker {

    /**
     * Data class representing a single usage of a message.
     *
     * @param source where the command lives when that is not a location's own command list, e.g.
     *               {@code Item 'brass key'} or {@code Arrival Process}; {@code null} for a location's own command
     */
    public record MessageUsage(String locationId, String locationDescription, String commandSpecification,
                               String actionType, String context, String source) implements TrackedUsage {

        /** A usage in a command of a location itself. */
        public MessageUsage(String locationId, String locationDescription, String commandSpecification,
                            String actionType, String context) {
            this(locationId, locationDescription, commandSpecification, actionType, context, null);
        }

        public String getDisplayText() {
            String location = locationDescription != null ? locationDescription : locationId;
            if (source == null) {
                return "Location: %s | Command: %s | %s".formatted(location, commandSpecification, context);
            }
            return "%s%s | Command: %s | %s".formatted(source, location == null ? "" : " in '" + location + "'",
                                                       commandSpecification, context);
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

        for (ActionScanner.ScannedAction scanned : ActionScanner.scan(adventureData, MessageActionData.class)) {
            if (messageId.equals(((MessageActionData) scanned.action()).getMessageId())) {
                ActionScanner.Origin origin = scanned.origin();
                usages.add(new MessageUsage(origin.locationId(), origin.locationDescription(),
                                            scanned.commandSpecification(), "Message Action",
                                            "Action #" + scanned.actionNumber(), origin.source()));
            }
        }
        return usages;
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
