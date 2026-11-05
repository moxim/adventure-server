package com.pdg.adventure.view.sound;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.PlayActionData;
import com.pdg.adventure.view.support.ActionScanner;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Finds where an adventure uses a sound: every PLAY action wherever a command can hold actions (see
 * {@link ActionScanner}: location commands, exits, items in locations and the pocket, and the workflow lists). A sound
 * that is used cannot be deleted.
 */
public final class SoundUsageTracker {

    private SoundUsageTracker() {
    }

    /**
     * One PLAY action that uses a sound.
     *
     * @param locationDescription the short description of the location the command belongs to; may be {@code null}
     * @param source              what holds the command when that is not a location's own command list; may be null
     */
    public record SoundUsage(String locationId, String locationDescription, String context, String source)
            implements TrackedUsage {

        @Override
        public String getDisplayText() {
            String locationName = locationDescription != null ? locationDescription : locationId;
            if (source == null) {
                return "Play Action: from '" + locationName + "' | " + context;
            }
            return "Play Action: from " + source + (locationName == null ? "" : " in '" + locationName + "'")
                   + " | " + context;
        }
    }

    public static List<SoundUsage> findSoundUsages(AdventureData adventureData, String soundId) {
        List<SoundUsage> usages = new ArrayList<>();
        if (adventureData == null || soundId == null || soundId.isEmpty()) {
            return usages;
        }
        for (ActionScanner.ScannedAction scanned : ActionScanner.scan(adventureData, PlayActionData.class)) {
            if (soundId.equals(((PlayActionData) scanned.action()).getSoundId())) {
                ActionScanner.Origin origin = scanned.origin();
                usages.add(new SoundUsage(origin.locationId(), origin.locationDescription(),
                                          "Command '" + scanned.commandSpecification() + "', Action #"
                                          + scanned.actionNumber(), origin.source()));
            }
        }
        return usages;
    }

    public static int countSoundUsages(AdventureData adventureData, String soundId) {
        return findSoundUsages(adventureData, soundId).size();
    }
}
