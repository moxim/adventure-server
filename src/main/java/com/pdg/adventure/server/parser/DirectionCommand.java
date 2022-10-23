package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;

public class DirectionCommand extends GenericCommand {
    private final Location destination;
    private final String verb;

    public DirectionCommand(CommandDescription aCommandDescription, MovePlayerAction anAction) {
        super(aCommandDescription.getVerb(), anAction);
        destination = anAction.getDestination();
        verb = aCommandDescription.getVerb();
    }

    public Location getDestination() {
        return destination;
    }

    public String getVerb() {
        return verb;
    }
}
