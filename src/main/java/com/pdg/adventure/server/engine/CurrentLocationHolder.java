package com.pdg.adventure.server.engine;

import java.util.function.Consumer;

import com.pdg.adventure.server.location.Location;

public class CurrentLocationHolder implements Consumer<Location> {
    @Override
    public void accept(Location aLocation) {
        Environment.setCurrentLocation(aLocation);
    }
}
