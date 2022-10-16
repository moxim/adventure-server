package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Direction;
import com.pdg.adventure.server.parser.DirectionCommand;
import com.pdg.adventure.server.support.ArticleProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class GenericDirection extends Item implements Direction {

    private final Location destination;
    private final boolean destinationMustBeMentioned;

    public GenericDirection(DirectionCommand aCommand, Location aLocation) {
        this(aCommand, aLocation, false);
    }

    public GenericDirection(DirectionCommand aCommand, Location aLocation,
                            boolean aFlagWhetherDestinationMustBeMentioned) {
        super(new DescriptionProvider(aLocation.getAdjective(), aLocation.getNoun()), false);
        destinationMustBeMentioned = aFlagWhetherDestinationMustBeMentioned;
        this.addCommand(aCommand);
        destination = aCommand.getDestination();
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
    public String getShortDescription() {
        if (destinationMustBeMentioned) {
            return ArticleProvider.prependUnknownArticle(constructDescriptionFromAdjectiveAndNoun());
        }
        return constructDescriptionFromAdjectiveAndNoun();
    }

/*
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
 */
}
