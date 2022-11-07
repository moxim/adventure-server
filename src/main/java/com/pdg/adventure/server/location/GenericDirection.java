package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Direction;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.DirectionCommand;
import com.pdg.adventure.server.support.ArticleProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class GenericDirection extends Item implements Direction {

    private final Location destination;
    private final CommandDescription description;
    private final boolean destinationMustBeMentioned;

    public GenericDirection(DirectionCommand aCommand) {
        this(aCommand, false);
    }

    public GenericDirection(DirectionCommand aCommand,
                            boolean aFlagWhetherDestinationMustBeMentioned) {
        super(new DescriptionProvider(aCommand.getDestination().getAdjective(), aCommand.getDestination().getNoun()), true);
        destinationMustBeMentioned = aFlagWhetherDestinationMustBeMentioned;
        destination = aCommand.getDestination();
        description = aCommand.getCommandDescription();
        this.addCommand(aCommand);
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
            return ArticleProvider.prependIndefiniteArticle(constructDescriptionFromAdjectiveAndNoun());
        }
        return description.getVerb();
    }

    @Override
    public String getLongDescription() {
        if (destinationMustBeMentioned) {
            String result = "You may " + description.getVerb();

            String noun = destination.getNoun();
            if (!Vocabulary.EMPTY_STRING.equals(noun)) {
                result += " the ";
            }

            result += constructDescriptionFromAdjectiveAndNoun();

            return result + ".";
        }
        return description.getVerb();
    }
}
