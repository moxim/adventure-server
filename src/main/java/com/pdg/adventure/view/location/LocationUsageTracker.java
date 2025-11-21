package com.pdg.adventure.view.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking location usage throughout an adventure.
 * Scans starting location, direction destinations, and movement actions to find references to specific locations.
 */
public class LocationUsageTracker {

    /**
     * Data class representing a single usage of a location.
     */
    public static class LocationUsage implements TrackedUsage {
        private String usageType;
        private final String sourceLocationId;
        private final String sourceLocationDescription;
        private final String context;
        private final String commandSpecification;

        public LocationUsage(String usageType, String sourceLocationId, String sourceLocationDescription,
                             String context, String commandSpecification) {
            this.usageType = usageType;
            this.sourceLocationId = sourceLocationId;
            this.sourceLocationDescription = sourceLocationDescription;
            this.context = context;
            this.commandSpecification = commandSpecification;
        }

        public String getUsageType() {
            return usageType;
        }

        public String getSourceLocationId() {
            return sourceLocationId;
        }

        public String getSourceLocationDescription() {
            return sourceLocationDescription;
        }

        public String getContext() {
            return context;
        }

        public String getCommandSpecification() {
            return commandSpecification;
        }

        public String getDisplayText() {
            StringBuilder sb = new StringBuilder();
            sb.append(usageType).append(": ");

            if ("Starting Location".equals(usageType)) {
                sb.append(context);
            } else if ("Direction".equals(usageType)) {
                String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
                sb.append("From '").append(locationName).append("' → ").append(context);
            } else {
                String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
                sb.append("From '").append(locationName).append("' | Command: ")
                  .append(commandSpecification).append(" | ").append(context);
            }

            return sb.toString();
        }
    }

    /**
     * Find all usages of a specific location in an adventure.
     *
     * @param adventureData The adventure to search
     * @param locationId    The location ID to find
     * @return List of LocationUsage objects describing where the location is referenced
     */
    public static List<LocationUsage> findLocationUsages(AdventureData adventureData, String locationId) {
        List<LocationUsage> usages = new ArrayList<>();

        if (adventureData == null || locationId == null || locationId.isEmpty()) {
            return usages;
        }

        // Check if this is the starting location
        if (locationId.equals(adventureData.getCurrentLocationId())) {
            usages.add(new LocationUsage(
                    "Starting Location",
                    null,
                    null,
                    "This is the starting location for the adventure",
                    null
            ));
        }

        // Scan through all locations
        Map<String, LocationData> locations = adventureData.getLocationData();
        if (locations != null) {
            for (Map.Entry<String, LocationData> locationEntry : locations.entrySet()) {
                LocationData location = locationEntry.getValue();
                String sourceLocationId = locationEntry.getKey();
                String sourceLocationDesc = location.getDescriptionData() != null ?
                                            location.getDescriptionData().getShortDescription() : null;

                // Check directions from this location
                checkDirections(location, sourceLocationId, sourceLocationDesc, locationId, usages);

                // Check actions in commands
                checkCommandActions(location, sourceLocationId, sourceLocationDesc, locationId, usages);
            }
        }

        return usages;
    }

    /**
     * Check if any directions from the source location lead to the target location.
     */
    private static void checkDirections(LocationData sourceLocation, String sourceLocationId,
                                        String sourceLocationDesc, String targetLocationId,
                                        List<LocationUsage> usages) {
        Set<DirectionData> directions = sourceLocation.getDirectionsData();
        if (directions != null) {
            for (DirectionData direction : directions) {
                if (targetLocationId.equals(direction.getDestinationId())) {
                    addUsagesForMatchingDirection(sourceLocationId, sourceLocationDesc, targetLocationId, usages,
                                                  direction);
                }
            }
        }
    }

    private static void addUsagesForMatchingDirection(final String sourceLocationId, final String sourceLocationDesc,
                                                      final String targetLocationId, final List<LocationUsage> usages,
                                                      final DirectionData direction) {
        String directionName = direction.getDescriptionData() != null ?
                               direction.getDescriptionData().getShortDescription() : "Unknown direction";
        if (directionName == null || directionName.isEmpty()) {
            addUsagesFromDirection(sourceLocationId, sourceLocationDesc, targetLocationId, usages, direction);
        } else {
            usages.add(new LocationUsage(
                    "Direction",
                    sourceLocationId,
                    sourceLocationDesc,
                    "Direction '" + directionName + "'",
                    null
            ));
        }
    }

    private static void addUsagesFromDirection(final String sourceLocationId, final String sourceLocationDesc,
                                               final String targetLocationId, final List<LocationUsage> usages,
                                               final DirectionData direction) {
        List<LocationUsage> fromDirections = new ArrayList<>();
        checkCommandsInDirection(direction, sourceLocationId, sourceLocationDesc, targetLocationId, fromDirections);
        for (var usage : fromDirections) {
            usage.usageType = ("in Direction: " + usage.getUsageType());
        }
        usages.addAll(fromDirections);
    }

    private static void checkCommandsInDirection(final DirectionData aDirection, String sourceLocationId,
                                                 String sourceLocationDesc, String targetLocationId,
                                                 List<LocationUsage> usages) {
        if (aDirection.getCommandProviderData() == null ||
            aDirection.getCommandProviderData().getAvailableCommands() == null) {
            return;
        }

        Map<String, CommandChainData> commands = aDirection.getCommandProviderData().getAvailableCommands();
        checkCommandChains(sourceLocationId, sourceLocationDesc, targetLocationId, usages, commands);

        checkCommand(sourceLocationId, sourceLocationDesc, targetLocationId, usages, aDirection.getCommandData(),
                     aDirection.getCommandData().getCommandDescription().getCommandSpecification());
    }

    /**
     * Check if any command actions in the location move to the target location.
     */
    private static void checkCommandActions(LocationData sourceLocation, String sourceLocationId,
                                            String sourceLocationDesc, String targetLocationId,
                                            List<LocationUsage> usages) {
        if (sourceLocation.getCommandProviderData() == null ||
            sourceLocation.getCommandProviderData().getAvailableCommands() == null) {
            return;
        }

        Map<String, CommandChainData> commands = sourceLocation.getCommandProviderData().getAvailableCommands();

        checkCommandChains(sourceLocationId, sourceLocationDesc, targetLocationId, usages, commands);
    }

    private static void checkCommandChains(final String sourceLocationId, final String sourceLocationDesc,
                                           final String targetLocationId, final List<LocationUsage> usages,
                                           final Map<String, CommandChainData> commands) {
        for (Map.Entry<String, CommandChainData> commandEntry : commands.entrySet()) {
            String commandSpec = commandEntry.getKey();
            CommandChainData chain = commandEntry.getValue();

            if (chain != null && chain.getCommands() != null) {
                for (CommandData command : chain.getCommands()) {
                    checkCommand(sourceLocationId, sourceLocationDesc, targetLocationId, usages, command, commandSpec);
                }
            }
        }
    }

    private static void checkCommand(final String sourceLocationId, final String sourceLocationDesc,
                                     final String targetLocationId, final List<LocationUsage> usages,
                                     final CommandData command, final String commandSpec) {
        // Check primary action
        if (command.getAction() != null) {
            checkMoveAction(command.getAction(), sourceLocationId, sourceLocationDesc,
                            commandSpec, "Primary Action", targetLocationId, usages);
        }

        // Check follow-up actions
        if (command.getFollowUpActions() != null) {
            int followUpIndex = 1;
            for (ActionData followUpAction : command.getFollowUpActions()) {
                checkMoveAction(followUpAction, sourceLocationId, sourceLocationDesc,
                                commandSpec, "Follow-up Action #" + followUpIndex,
                                targetLocationId, usages);
                followUpIndex++;
            }
        }
    }

    /**
     * Check if an action is a MovePlayerAction targeting the location and add to usages list if it does.
     */
    private static void checkMoveAction(ActionData action, String sourceLocationId, String sourceLocationDesc,
                                        String commandSpec, String context, String targetLocationId,
                                        List<LocationUsage> usages) {
        if (action instanceof MovePlayerActionData moveAction && targetLocationId.equals(moveAction.getLocationId())) {
            usages.add(new LocationUsage(
                    "Move Action",
                    sourceLocationId,
                    sourceLocationDesc,
                    context,
                    commandSpec
            ));
        }

    }

    /**
     * Count how many times a location is referenced in an adventure.
     *
     * @param adventureData The adventure to search
     * @param locationId    The location ID to count
     * @return Number of times the location is referenced
     */
    public static int countLocationUsages(AdventureData adventureData, String locationId) {
        return findLocationUsages(adventureData, locationId).size();
    }

    /**
     * Check if a location is referenced anywhere in the adventure.
     *
     * @param adventureData The adventure to search
     * @param locationId    The location ID to check
     * @return true if the location is referenced at least once
     */
    public static boolean isLocationUsed(AdventureData adventureData, String locationId) {
        return countLocationUsages(adventureData, locationId) > 0;
    }
}
