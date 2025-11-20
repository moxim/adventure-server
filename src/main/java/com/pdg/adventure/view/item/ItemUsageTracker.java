package com.pdg.adventure.view.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.*;
import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking item usage throughout an adventure.
 * Scans command actions to find references to specific items.
 */
public class ItemUsageTracker {

    /**
     * Data class representing a single usage of an item.
     */
    public static class ItemUsage implements TrackedUsage {
        private final String usageType;
        private final String sourceLocationId;
        private final String sourceLocationDescription;
        private final String context;
        private final String commandSpecification;

        public ItemUsage(String usageType, String sourceLocationId, String sourceLocationDescription,
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

            if ("Location Item".equals(usageType)) {
                sb.append("In location '").append(sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId).append("'");
            } else {
                String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
                sb.append("In '").append(locationName).append("' | Command: ")
                  .append(commandSpecification).append(" | ").append(context);
            }

            return sb.toString();
        }
    }

    /**
     * Find all usages of a specific item in an adventure.
     * @param adventureData The adventure to search
     * @param itemId The item ID to find
     * @return List of ItemUsage objects describing where the item is referenced
     */
    public static List<ItemUsage> findItemUsages(AdventureData adventureData, String itemId) {
        List<ItemUsage> usages = new ArrayList<>();

        if (adventureData == null || itemId == null || itemId.isEmpty()) {
            return usages;
        }

        // Scan through all locations
        Map<String, LocationData> locations = adventureData.getLocationData();
        if (locations != null) {
            for (Map.Entry<String, LocationData> locationEntry : locations.entrySet()) {
                LocationData location = locationEntry.getValue();
                String sourceLocationId = locationEntry.getKey();
                String sourceLocationDesc = location.getDescriptionData() != null ?
                        location.getDescriptionData().getShortDescription() : null;

                // Check if item is in this location's ItemContainer
                if (location.getItemContainerData() != null &&
                    location.getItemContainerData().getItems() != null) {
                    boolean itemInLocation = location.getItemContainerData().getItems().stream()
                            .anyMatch(item -> item != null && itemId.equals(item.getId()));

                    if (itemInLocation) {
                        usages.add(new ItemUsage(
                                "Location Item",
                                sourceLocationId,
                                sourceLocationDesc,
                                "Item is in this location",
                                null
                        ));
                    }
                }

                // Check command actions in this location
                checkCommandActions(location, sourceLocationId, sourceLocationDesc, itemId, usages);
            }
        }

        return usages;
    }

    /**
     * Check if any command actions in the location reference the target item.
     */
    private static void checkCommandActions(LocationData sourceLocation, String sourceLocationId,
                                          String sourceLocationDesc, String targetItemId,
                                          List<ItemUsage> usages) {
        if (sourceLocation.getCommandProviderData() == null ||
            sourceLocation.getCommandProviderData().getAvailableCommands() == null) {
            return;
        }

        Map<String, CommandChainData> commands = sourceLocation.getCommandProviderData().getAvailableCommands();

        for (Map.Entry<String, CommandChainData> commandEntry : commands.entrySet()) {
            String commandSpec = commandEntry.getKey();
            CommandChainData chain = commandEntry.getValue();

            if (chain != null && chain.getCommands() != null) {
                for (CommandData command : chain.getCommands()) {
                    // Check primary action
                    if (command.getAction() != null) {
                        checkItemAction(command.getAction(), sourceLocationId, sourceLocationDesc,
                                commandSpec, "Primary Action", targetItemId, usages);
                    }

                    // Check follow-up actions
                    if (command.getFollowUpActions() != null) {
                        int followUpIndex = 1;
                        for (ActionData followUpAction : command.getFollowUpActions()) {
                            checkItemAction(followUpAction, sourceLocationId, sourceLocationDesc,
                                    commandSpec, "Follow-up Action #" + followUpIndex,
                                    targetItemId, usages);
                            followUpIndex++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if an action references the target item and add to usages list if it does.
     */
    private static void checkItemAction(BasicData action, String sourceLocationId, String sourceLocationDesc,
                                       String commandSpec, String context, String targetItemId,
                                       List<ItemUsage> usages) {
        String thingId = null;
        String actionType = null;

        if (action instanceof TakeActionData takeAction) {
            thingId = takeAction.getThingId();
            actionType = "Take Action";
        } else if (action instanceof DropActionData dropAction) {
            thingId = dropAction.getThingId();
            actionType = "Drop Action";
        } else if (action instanceof WearActionData wearAction) {
            thingId = wearAction.getThingId();
            actionType = "Wear Action";
        } else if (action instanceof MoveItemActionData moveAction) {
            thingId = moveAction.getThingId();
            actionType = "Move Item Action";
        } else if (action instanceof CreateActionData createAction) {
            thingId = createAction.getThingId();
            actionType = "Create Action";
        } else if (action instanceof DestroyActionData destroyAction) {
            thingId = destroyAction.getThingId();
            actionType = "Destroy Action";
        } else if (action instanceof RemoveActionData removeAction) {
            thingId = removeAction.getThingId();
            actionType = "Remove Action";
        }

        if (thingId != null && targetItemId.equals(thingId)) {
            usages.add(new ItemUsage(
                    actionType,
                    sourceLocationId,
                    sourceLocationDesc,
                    context,
                    commandSpec
            ));
        }
    }

    /**
     * Count how many times an item is referenced in an adventure.
     * @param adventureData The adventure to search
     * @param itemId The item ID to count
     * @return Number of times the item is referenced
     */
    public static int countItemUsages(AdventureData adventureData, String itemId) {
        return findItemUsages(adventureData, itemId).size();
    }

    /**
     * Find all non-location-container usages of an item (e.g., command actions).
     * This is useful for showing users what references prevent an item from being deleted.
     * @param adventureData The adventure to search
     * @param itemId The item ID to find
     * @return List of ItemUsage objects excluding location container references
     */
    public static List<ItemUsage> findItemUsagesExcludingLocationContainers(AdventureData adventureData, String itemId) {
        List<ItemUsage> allUsages = findItemUsages(adventureData, itemId);
        return allUsages.stream()
                .filter(usage -> !"Location Item".equals(usage.getUsageType()))
                .toList();
    }

    /**
     * Check if an item is referenced anywhere in the adventure.
     * @param adventureData The adventure to search
     * @param itemId The item ID to check
     * @return true if the item is referenced at least once
     */
    public static boolean isItemUsed(AdventureData adventureData, String itemId) {
        return countItemUsages(adventureData, itemId) > 0;
    }
}
