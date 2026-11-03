package com.pdg.adventure.view.location;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;

/**
 * The rules for making a location, shared by the location editor and the world map so both fill a new location the
 * same way. Works on the data only and never saves; the caller saves the adventure, which stores its new locations.
 */
public final class LocationCreator {

    private LocationCreator() {
    }

    /**
     * Makes a location with the given noun (required), optional adjective and short description, and adds it to the
     * adventure. It is not placed on the world map.
     */
    public static LocationData create(AdventureData anAdventure, Word aNoun, Word anAdjective,
                                      String aShortDescription) {
        final LocationData location = new LocationData();
        final DescriptionData description = location.getDescriptionData();
        description.setNoun(aNoun);
        description.setAdjective(anAdjective);
        if (aShortDescription != null && !aShortDescription.isBlank()) {
            description.setShortDescription(aShortDescription.strip());
        }
        syncItemContainerDescription(location);
        anAdventure.getLocationData().put(location.getId(), location);
        return location;
    }

    /** The items lying in a location are described by the location's own noun and adjective. */
    public static void syncItemContainerDescription(LocationData aLocation) {
        final DescriptionData locationDescription = aLocation.getDescriptionData();
        final DescriptionData containerDescription = aLocation.getItemContainerData().getDescriptionData();
        containerDescription.setAdjective(locationDescription.getAdjective());
        containerDescription.setNoun(locationDescription.getNoun());
    }
}
