package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Direction;
import com.pdg.adventure.server.parser.DirectionCommand;
import com.pdg.adventure.server.support.ArticleProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class GenericDirection extends Item implements Direction {

    private final Location destination;
    private final String verb;
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
        verb = aCommand.getVerb();
    }

    public Location getDestination() {
        return destination;
    }

    private String constructDescriptionFromAdjectiveAndNoun() {
        String result = "";
        String adjective = destination.getAdjective();
        if (!Vocabulary.EMPTY_STRING.equals(adjective)) {
            result += destination.getAdjective() + " ";
        }

        String noun = destination.getNoun();
        if (!Vocabulary.EMPTY_STRING.equals(noun)) {
            result += noun;
        }
        return result;
    }

    @Override
    public String getShortDescription() {
        if (destinationMustBeMentioned) {
            return ArticleProvider.prependUnknownArticle(constructDescriptionFromAdjectiveAndNoun());
        }
        return verb;
    }

    @Override
    public String getLongDescription() {
        if (destinationMustBeMentioned) {
            String result = "You can " + verb;

            String noun = destination.getNoun();
            if (!Vocabulary.EMPTY_STRING.equals(noun)) {
                result += " the ";
            }

            result += constructDescriptionFromAdjectiveAndNoun();

            return result + ".";
        }
        return verb;
    }
}
