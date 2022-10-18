package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.Environment;

public class ItemAtCondition implements PreCondition {
    private final Location location;

    public ItemAtCondition(Location aLocation) {
        location = aLocation;
    }

    @Override
    public boolean isValid() {
        return Environment.getCurrentLocation().equals(location);
    }
}
