package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;

public class DirectionCommand extends GenericCommand {
    private final Location destination;

    public DirectionCommand(CommandDescription aCommandDescription, Location aLocation) {
        super(aCommandDescription, new MovePlayerAction(aLocation));
        destination = aLocation;
    }

    public Location getDestination() {
        return destination;
    }
}
