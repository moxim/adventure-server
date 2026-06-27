package com.pdg.adventure.view.location;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;

public class LocationProvider {

    private static AdventureService adventureService;

    private LocationProvider() {
        // do not instantiate
    }

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
        return new LocationData();
    }

    private static LocationData setUpLoading(String aLocationId) {
        return adventureService.findLocationById(aLocationId);
    }
}
