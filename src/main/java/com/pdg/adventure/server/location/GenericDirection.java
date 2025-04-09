package com.pdg.adventure.server.location;

import lombok.Getter;

import java.util.Map;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Direction;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.support.ArticleProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.Thing;

public class GenericDirection
        extends Thing
        implements Direction {

    @Getter
    private final String destinationId;
    @Getter
    private final boolean destinationMustBeMentioned;

    private final CommandDescription description;

    private Map<String, Location> locations;
    private Container partentContainer;

    public GenericDirection(Map<String, Location> aLocationMap, Command aCommand, String aDestinationId) {
        this(aLocationMap, aCommand, aDestinationId, false);
    }

    public GenericDirection(Map<String, Location> aLocationMap, Command aCommand, String aDestinationId,
                            boolean aFlagWhetherDestinationMustBeMentioned) {
        super(new DescriptionProvider(aLocationMap.get(aDestinationId).getAdjective(), aLocationMap.get(aDestinationId).getNoun()));
        locations = aLocationMap;
        destinationMustBeMentioned = aFlagWhetherDestinationMustBeMentioned;
        destinationId = aDestinationId;
        description = aCommand.getDescription();
        addCommand(aCommand);
    }

    private String constructDescriptionFromAdjectiveAndNoun() {
        String result = "";
        String adjective = getDestination().getAdjective();
        if (!VocabularyData.EMPTY_STRING.equals(adjective)) {
            result += getDestination().getAdjective() + " ";
        }

        String noun = getDestination().getNoun();
        if (!VocabularyData.EMPTY_STRING.equals(noun)) {
            result += noun;
        }
        return result;
    }

    @Override
    public String getShortDescription() {
        String constructedDescription = constructDescriptionFromAdjectiveAndNoun();
        if (destinationMustBeMentioned) {
            if (!VocabularyData.EMPTY_STRING.equals(constructedDescription)) {
                return ArticleProvider.prependIndefiniteArticle(constructedDescription);
            } else {
                return description.getVerb() + " " + ArticleProvider.prependDefiniteArticle(description.getNoun());
            }
        }
        return description.getVerb();
    }

    @Override
    public String getLongDescription() {
        if (destinationMustBeMentioned) {
            String result = "You may " + description.getVerb();

            String noun = getDestination().getNoun();
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

    @Override
    public boolean isContainable() {
        return true;
    }

    @Override
    public Container getParentContainer() {
        return partentContainer;
    }

    @Override
    public void setParentContainer(Container aContainer) {
        partentContainer = aContainer;
    }

    private Location getDestination() {
        return locations.get(destinationId);
    }
}
