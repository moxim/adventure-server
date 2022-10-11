package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.ArticleProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.Collections;
import java.util.List;

public class Direction extends GenericCommand implements Describable {
    private final Location destination;
    private final String command;
    private final boolean destinationMustBeMentioned;

    public Direction(String aCommand, Location aDestination, Vocabulary aVocabulary) {
        this(aCommand, aDestination, false, aVocabulary);
    }

    public Direction(String aCommand, Location aDestination, boolean aFlagWhetherDestinationMustBeMentioned, Vocabulary aVocabulary) {
        super(aCommand, null, aVocabulary);
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
            return ArticleProvider.prependUnknownArticle(constructDescriptionFromAdjectiveAndNoun());
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

    @Override
    public void executeAction() {
        Environment.tell("You move to the " + destination.getNoun());
        Environment.setCurrentLocation(destination);
    }

    @Override
    public List<Command> getCommands() {
        return Collections.emptyList();
    }

    @Override
    public void addCommand(Command aCommand) {
        throw new UnsupportedOperationException("Can't add Commands to a Direction.");
    }

    @Override
    public void removeCommand(Command aCommand) {
        throw new UnsupportedOperationException("Can't remove Commands from a Direction.");
    }

    @Override
    public boolean applyCommand(String aVerb) {
        executeAction();
        return true;
    }

    public boolean applyCommand(CommandDescription aCommand) {
        final String verb = aCommand.getVerb();
        final String adjective = aCommand.getAdjective();
        final String noun = aCommand.getNoun();

        if (verb.equals(command) && noun.equals(destination.getNoun()) &&
                (adjective.isEmpty() || adjective.equals(getAdjective()))) {
            return execute();
        }
        return false;
    }
}
