package com.pdg.adventure.server.location;

import com.pdg.adventure.server.action.MoveAction;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.Environment;

public class Direction extends GenericCommand implements Describable {
    private final Location destination;
    private final String command;
    private final boolean destinationMustBeMentioned;

    public Direction(String aCommand, Location aDestination) {
        this(aCommand, aDestination, false);
    }

    public Direction(String aCommand, Location aDestination, boolean aFlagWhetherDestinationMustBeMentioned) {
        super(aCommand, new MoveAction(null, aDestination.getContainer()));
        destination = aDestination;
        command = aCommand;
        destinationMustBeMentioned = aFlagWhetherDestinationMustBeMentioned;
    }

    public Location getDestination() {
        return destination;
    }

    private String constructDescriptionFromAdjectiveAndNoun() {
        String result = "";
        String adjective = destination.getAdjective();
        if (!Environment.EMPTY_STRING.equals(adjective)) {
            result += destination.getAdjective() + " ";
        }

        String noun = destination.getNoun();
        if (!Environment.EMPTY_STRING.equals(noun)) {
            result += noun;
        }
        return result;
    }

    @Override
    public String getAdjective() {
        return destination.getAdjective();
    }

    @Override
    public String getNoun() {
        return destination.getNoun();
    }

    @Override
    public String getShortDescription() {
        if (destinationMustBeMentioned) {
            return constructDescriptionFromAdjectiveAndNoun();
        }
        return command;
    }

    @Override
    public String getLongDescription() {
        if (destinationMustBeMentioned) {
            String result = "You can " + command;

            String noun = destination.getNoun();
            if (!Environment.EMPTY_STRING.equals(noun)) {
                result += " the ";
            }

            result += constructDescriptionFromAdjectiveAndNoun();

            return result;
        }
        return command;
    }
}
