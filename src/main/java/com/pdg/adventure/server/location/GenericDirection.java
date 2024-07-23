package com.pdg.adventure.server.location;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Direction;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.support.ArticleProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.Item;
import lombok.Getter;

public class GenericDirection extends Item implements Direction {

    @Getter
    private final Location destination;
    private final CommandDescription description;

    @Getter
    private final boolean destinationMustBeMentioned;

    public GenericDirection(Command aCommand, Location aDestination) {
        this(aCommand, aDestination, false);
    }

    public GenericDirection(Command aCommand, Location aDestination,
                            boolean aFlagWhetherDestinationMustBeMentioned) {
        super(new DescriptionProvider(aDestination.getAdjective(), aDestination.getNoun()), true);
        destinationMustBeMentioned = aFlagWhetherDestinationMustBeMentioned;
        destination = aDestination;
        description = aCommand.getDescription();
        addCommand(aCommand);
    }

    private String constructDescriptionFromAdjectiveAndNoun() {
        String result = "";
        String adjective = destination.getAdjective();
        if (!VocabularyData.EMPTY_STRING.equals(adjective)) {
            result += destination.getAdjective() + " ";
        }

        String noun = destination.getNoun();
        if (!VocabularyData.EMPTY_STRING.equals(noun)) {
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
            if (!VocabularyData.EMPTY_STRING.equals(noun)) {
                result += " the ";
            }

            result += constructDescriptionFromAdjectiveAndNoun();

            return result + ".";
        }
        return description.getVerb();
    }

    public Command getCommand() {
        // TODO: really??
        return getCommands().get(0);
    }
}
