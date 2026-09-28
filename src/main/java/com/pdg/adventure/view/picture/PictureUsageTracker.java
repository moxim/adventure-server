package com.pdg.adventure.view.picture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking picture usage throughout an adventure. Scans every location's
 * default picture, and every PICTURE action attached to a location's own commands or to any of
 * its exits' commands. Mirrors {@link com.pdg.adventure.view.location.LocationUsageTracker}'s
 * scope exactly (same object graph, same coverage) - it does not scan item commands, matching
 * that class's existing, established scope rather than expanding it for this feature.
 */
public class PictureUsageTracker {

    public static class PictureUsage implements TrackedUsage {
        private final String usageType;
        private final String sourceLocationId;
        private final String sourceLocationDescription;
        private final String context;

        public PictureUsage(String usageType, String sourceLocationId, String sourceLocationDescription,
                            String context) {
            this.usageType = usageType;
            this.sourceLocationId = sourceLocationId;
            this.sourceLocationDescription = sourceLocationDescription;
            this.context = context;
        }

        public String getUsageType() {
            return usageType;
        }

        @Override
        public String getDisplayText() {
            if ("Location Default Picture".equals(usageType)) {
                String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
                return usageType + ": is the default picture for '" + locationName + "'";
            }
            String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
            return usageType + ": from '" + locationName + "' | " + context;
        }
    }

    public static List<PictureUsage> findPictureUsages(AdventureData adventureData, String pictureId) {
        List<PictureUsage> usages = new ArrayList<>();

        if (adventureData == null || pictureId == null || pictureId.isEmpty()) {
            return usages;
        }

        Map<String, LocationData> locations = adventureData.getLocationData();
        if (locations != null) {
            for (Map.Entry<String, LocationData> entry : locations.entrySet()) {
                LocationData location = entry.getValue();
                String sourceLocationId = entry.getKey();
                String sourceLocationDesc = location.getDescriptionData() != null
                        ? location.getDescriptionData().getShortDescription() : null;

                if (pictureId.equals(location.getPictureId())) {
                    usages.add(new PictureUsage("Location Default Picture", sourceLocationId, sourceLocationDesc,
                                                null));
                }

                checkLocationCommands(location, sourceLocationId, sourceLocationDesc, pictureId, usages);
                checkDirectionCommands(location, sourceLocationId, sourceLocationDesc, pictureId, usages);
            }
        }

        return usages;
    }

    private static void checkLocationCommands(LocationData sourceLocation, String sourceLocationId,
                                              String sourceLocationDesc, String pictureId,
                                              List<PictureUsage> usages) {
        if (sourceLocation.getCommandProviderData() == null
            || sourceLocation.getCommandProviderData().getAvailableCommands() == null) {
            return;
        }
        for (CommandChainData chain : sourceLocation.getCommandProviderData().getAvailableCommands().values()) {
            if (chain == null || chain.getCommands() == null) {
                continue;
            }
            for (CommandData command : chain.getCommands()) {
                checkCommandActions(command, sourceLocationId, sourceLocationDesc, pictureId, usages);
            }
        }
    }

    private static void checkDirectionCommands(LocationData sourceLocation, String sourceLocationId,
                                               String sourceLocationDesc, String pictureId,
                                               List<PictureUsage> usages) {
        if (sourceLocation.getDirectionsData() == null) {
            return;
        }
        for (DirectionData direction : sourceLocation.getDirectionsData()) {
            if (direction.getCommandData() != null) {
                checkCommandActions(direction.getCommandData(), sourceLocationId, sourceLocationDesc, pictureId,
                                    usages);
            }
        }
    }

    private static void checkCommandActions(CommandData command, String sourceLocationId, String sourceLocationDesc,
                                            String pictureId, List<PictureUsage> usages) {
        String commandSpec = command.getCommandDescription().getCommandSpecification();
        int actionIndex = 1;
        for (ActionData action : command.getActions()) {
            if (action instanceof PictureActionData pictureAction && pictureId.equals(pictureAction.getPictureId())) {
                usages.add(new PictureUsage("Picture Action", sourceLocationId, sourceLocationDesc,
                                            "Command '" + commandSpec + "', Action #" + actionIndex));
            }
            actionIndex++;
        }
    }

    public static int countPictureUsages(AdventureData adventureData, String pictureId) {
        return findPictureUsages(adventureData, pictureId).size();
    }

    public static boolean isPictureUsed(AdventureData adventureData, String pictureId) {
        return countPictureUsages(adventureData, pictureId) > 0;
    }
}
