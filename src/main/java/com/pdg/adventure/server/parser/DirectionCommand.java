package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;

public class DirectionCommand extends GenericCommand {
    private final Location destination;
    private final CommandDescription commandDescription;

    public DirectionCommand(CommandDescription aCommandDescription, Location aLocation) {
        super(aCommandDescription, new MovePlayerAction(aLocation));
        destination = aLocation;
        commandDescription = aCommandDescription;
    }

    public Location getDestination() {
        return destination;
    }

    public CommandDescription getCommandDescription() {
        return commandDescription;
    }
}
