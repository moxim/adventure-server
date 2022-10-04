package com.pdg.adventure.server.location;

import com.pdg.adventure.server.action.CommandDescription;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.support.Environment;

public class Direction implements Describable {
    private final Container location;
    private final CommandDescription command;

    public Direction(Container aLocation, CommandDescription aCommand) {
        location = aLocation;
        command = aCommand;
    }

    public String getDescription() {
        String verb = "You can " + command.getVerb();
        String result = verb;

        String noun = command.getNoun();
        if (!Environment.EMPTY_STRING.equals(noun)) {
            result += " the ";
        }

        result += constructDescriptionFromAdjectiveAndNoun();

        return result;
    }

    private String constructDescriptionFromAdjectiveAndNoun() {
        String result = "";
        String adjective = command.getAdjective();
        if (!Environment.EMPTY_STRING.equals(adjective)) {
            result += command.getAdjective() + " ";
        }

        String noun = command.getNoun();
        if (!Environment.EMPTY_STRING.equals(noun)) {
            result += noun;
        }
        return result;
    }

    @Override
    public String getAdjective() {
        return command.getAdjective();
    }

    @Override
    public String getNoun() {
        return command.getNoun();
    }

    @Override
    public String getShortDescription() {
        return constructDescriptionFromAdjectiveAndNoun();
    }

    @Override
    public String getLongDescription() {
        return getDescription();
    }
}
