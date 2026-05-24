package com.pdg.adventure.server.location;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.parser.CommandHandler;
import com.pdg.adventure.server.support.ArticleProvider;

public class GenericDirection implements Direction {

    private String id;
    private final transient CommandHandler commandHandler;
    @Getter
    private final String destinationId;
    @Getter
    private final boolean destinationMustBeMentioned;
    private final CommandDescription description;
    private final Map<String, Location> locations;
    private Container parentContainer;

    public GenericDirection(Map<String, Location> aLocationMap, Command aCommand, String aDestinationId) {
        this(aLocationMap, aCommand, aDestinationId, false);
    }

    public GenericDirection(Map<String, Location> aLocationMap, Command aCommand, String aDestinationId,
                            boolean aFlagWhetherDestinationMustBeMentioned) {
        commandHandler = new CommandHandler();
        commandHandler.addCommand(aCommand);
        description = aCommand.getDescription();
        destinationId = aDestinationId;
        destinationMustBeMentioned = aFlagWhetherDestinationMustBeMentioned;
        locations = aLocationMap;
        id = UUID.randomUUID().toString();
    }

    // -------------------------------------------------------------------------
    // Ided
    // -------------------------------------------------------------------------

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    // -------------------------------------------------------------------------
    // Describable — derived from the destination location or command description
    // -------------------------------------------------------------------------

    @Override
    public String getAdjective() {
        return getDestination().getAdjective();
    }

    @Override
    public String getNoun() {
        return getDestination().getNoun();
    }

    @Override
    public String getBasicDescription() {
        return description.getBasicDescription();
    }

    @Override
    public String getEnrichedBasicDescription() {
        return description.getEnrichedBasicDescription();
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
        return buildCommandDescription();
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
        return "You may " + buildCommandDescription() + ".";
    }

    @Override
    public String getEnrichedShortDescription() {
        return getShortDescription();
    }

    public void setExamineFallback(String aVerb, Supplier<String> aDescription) {
        commandHandler.setExamineFallback(aVerb, aDescription);
    }

    // -------------------------------------------------------------------------
    // HasCommands — delegated to CommandHandler
    // -------------------------------------------------------------------------

    @Override
    public List<Command> getCommands() {
        return commandHandler.getCommands();
    }

    @Override
    public void addCommand(Command aCommand) {
        commandHandler.addCommand(aCommand);
    }

    @Override
    public void removeCommand(Command aCommand) {
        commandHandler.removeCommand(aCommand);
    }

    @Override
    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        return commandHandler.applyCommand(aCommandDescription);
    }

    @Override
    public boolean hasVerb(String aVerb) {
        return commandHandler.hasVerb(aVerb);
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        return commandHandler.getMatchingCommandChain(aCommandDescription);
    }

    @Override
    public CommandProvider getCommandProvider() {
        return commandHandler.getCommandProvider();
    }

    // -------------------------------------------------------------------------
    // Containable
    // -------------------------------------------------------------------------

    @Override
    public boolean isContainable() {
        return true;
    }

    @Override
    public Container getParentContainer() {
        return parentContainer;
    }

    @Override
    public void setParentContainer(Container aContainer) {
        parentContainer = aContainer;
    }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof GenericDirection that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GenericDirection{id='" + id + "', destinationId='" + destinationId + "'}";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String constructDescriptionFromAdjectiveAndNoun() {
        String result = "";
        String adjective = getDestination().getAdjective();
        if (!VocabularyData.EMPTY_STRING.equals(adjective)) {
            result += adjective + " ";
        }
        String noun = getDestination().getNoun();
        if (!VocabularyData.EMPTY_STRING.equals(noun)) {
            result += noun;
        }
        return result;
    }

    private String buildCommandDescription() {
        StringBuilder result = new StringBuilder(description.getVerb());
        String adjective = description.getAdjective();
        String noun = description.getNoun();
        if (!VocabularyData.EMPTY_STRING.equals(noun)) {
            result.append(" the");
            if (!VocabularyData.EMPTY_STRING.equals(adjective)) {
                result.append(" ").append(adjective);
            }
            result.append(" ").append(noun);
        }
        return result.toString();
    }

    private Location getDestination() {
        return locations.get(destinationId);
    }
}
