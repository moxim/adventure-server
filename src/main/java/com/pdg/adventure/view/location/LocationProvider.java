package com.pdg.adventure.view.location;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;

public class LocationProvider {

    private static AdventureService adventureService;

    public static LocationData getLocation(AdventureService anAdventureService, String aLocationId) {
        adventureService = anAdventureService;
        LocationData locationData;
        if (aLocationId != null) {
            locationData = setUpLoading(aLocationId);
        } else {
            locationData = setUpNewEdit();
        }
        return locationData;
    }

    private static LocationData setUpNewEdit() {
        LocationData locationData = new LocationData();
        return locationData;
    }

    private static LocationData setUpLoading(String aLocationId) {
        LocationData locationData = adventureService.findLocationById(aLocationId);
        return locationData;
    }
}
