package com.pdg.adventure.server.action;

import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.Environment;

public class MovePlayerAction extends AbstractAction {
    private final Location destination;

    public MovePlayerAction(Location aDestination) {
        destination = aDestination;
    }

    @Override
    public void execute() {
        Environment.setCurrentLocation(destination);
    }

    public Location getDestination() {
        return destination;
    }
}
