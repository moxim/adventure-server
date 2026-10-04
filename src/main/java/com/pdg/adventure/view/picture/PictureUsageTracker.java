package com.pdg.adventure.view.picture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.view.support.ActionScanner;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking picture usage throughout an adventure. Scans every location's default
 * picture, and every PICTURE action wherever a command can hold actions (see {@link ActionScanner}):
 * location commands, exits, items (in locations or the player's pocket) and the workflow lists.
 */
public class PictureUsageTracker {
    private static final String DEFAULT_PICTURE_TEXT = "Location Default Picture";

    public static class PictureUsage implements TrackedUsage {
        private final String usageType;
        private final String sourceLocationId;
        private final String sourceLocationDescription;
        private final String context;
        private final String source;

        /** A usage on a location itself: its default picture, or an action in one of its own commands. */
        public PictureUsage(String usageType, String sourceLocationId, String sourceLocationDescription,
                            String context) {
            this(usageType, sourceLocationId, sourceLocationDescription, context, null);
        }

        /**
         * @param source what holds the command when that is not a location's own command list, e.g.
         *               {@code Item 'brass key'} or {@code Arrival Process}; {@code null} otherwise
         */
        public PictureUsage(String usageType, String sourceLocationId, String sourceLocationDescription,
                            String context, String source) {
            this.usageType = usageType;
            this.sourceLocationId = sourceLocationId;
            this.sourceLocationDescription = sourceLocationDescription;
            this.context = context;
            this.source = source;
        }

        public String getUsageType() {
            return usageType;
        }

        @Override
        public String getDisplayText() {
            String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
            if (DEFAULT_PICTURE_TEXT.equals(usageType)) {
                return usageType + ": is the default picture for '" + locationName + "'";
            }
            if (source == null) {
                return usageType + ": from '" + locationName + "' | " + context;
            }
            return usageType + ": from " + source + (locationName == null ? "" : " in '" + locationName + "'")
                   + " | " + context;
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
                if (location != null && pictureId.equals(location.getPictureId())) {
                    String locationDesc = location.getDescriptionData() != null
                            ? location.getDescriptionData().getShortDescription() : null;
                    usages.add(new PictureUsage(DEFAULT_PICTURE_TEXT, entry.getKey(), locationDesc, null));
                }
            }
        }

        for (ActionScanner.ScannedAction scanned : ActionScanner.scan(adventureData, PictureActionData.class)) {
            if (pictureId.equals(((PictureActionData) scanned.action()).getPictureId())) {
                ActionScanner.Origin origin = scanned.origin();
                usages.add(new PictureUsage("Picture Action", origin.locationId(), origin.locationDescription(),
                                            "Command '" + scanned.commandSpecification() + "', Action #"
                                            + scanned.actionNumber(), origin.source()));
            }
        }
        return usages;
    }

    public static int countPictureUsages(AdventureData adventureData, String pictureId) {
        return findPictureUsages(adventureData, pictureId).size();
    }

    public static boolean isPictureUsed(AdventureData adventureData, String pictureId) {
        return countPictureUsages(adventureData, pictureId) > 0;
    }
}
