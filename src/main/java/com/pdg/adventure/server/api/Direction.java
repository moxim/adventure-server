package com.pdg.adventure.server.api;

import com.pdg.adventure.server.location.Location;

public interface Direction extends Actionable, Describable {
    Location getDestination();
}
