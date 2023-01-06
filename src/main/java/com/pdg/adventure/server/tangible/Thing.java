package com.pdg.adventure.server.tangible;

import com.pdg.adventure.api.Actionable;
import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Thing implements Describable, Actionable {

    private final CommandProvider commandProvider;
    private final DescriptionProvider descriptionProvider;
    private final UUID id;

    public Thing(DescriptionProvider aDescriptionProvider) {
        commandProvider = new CommandProvider();
        descriptionProvider = aDescriptionProvider;
        id = UUID.randomUUID();
    }

    @Override
    public String getAdjective() {
        return descriptionProvider.getAdjective();
    }

    @Override
    public String getNoun() {
        return descriptionProvider.getNoun();
    }

    @Override
    public String getBasicDescription() {
        return descriptionProvider.getBasicDescription();
    }

    @Override
    public String getEnrichedBasicDescription() {
        return descriptionProvider.getEnrichedBasicDescription();
    }

    @Override
    public String getShortDescription() {
        return descriptionProvider.getShortDescription();
    }

    public void setShortDescription(String aShortDescription) {
        descriptionProvider.setShortDescription(aShortDescription);
    }

    @Override
    public String getLongDescription() {
        return descriptionProvider.getLongDescription();
    }

    @Override
    public String getEnrichedShortDescription() {
        return descriptionProvider.getEnrichedShortDescription(getShortDescription());
    }

    public void setLongDescription(String aLongDescription) {
        descriptionProvider.setLongDescription(aLongDescription);
    }

    public ExecutionResult applyCommand(CommandDescription aCommand) {
        return commandProvider.applyCommand(aCommand);
    }

    @Override
    public boolean hasVerb(String aVerb) {
        return commandProvider.hasVerb(aVerb);
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        return commandProvider.getMatchingCommandChain(aCommandDescription);
    }

    @Override
    public List<Command> getCommands() {
        return new ArrayList<>(commandProvider.getCommands());
    }

    @Override
    public void addCommand(Command aCommand) {
        commandProvider.addCommand(aCommand);
    }

    @Override
    public void removeCommand(Command aCommand) {
        commandProvider.removeCommand(aCommand);
    }

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof Thing thing)) return false;
        return id.equals(thing.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getShortDescription();
    }
}
