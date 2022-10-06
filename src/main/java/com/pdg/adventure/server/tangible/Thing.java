package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Actionable;
import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.support.CommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Thing implements Describable, Actionable {

    private final DescriptionProvider descriptionProvider;
    private final CommandProvider commandProvider;

    private final UUID id;

    public Thing(DescriptionProvider aDescriptionProvider) {
        descriptionProvider = aDescriptionProvider;
        commandProvider = new CommandProvider();
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

    public void setLongDescription(String aLongDescription) {
        descriptionProvider.setLongDescription(aLongDescription);
    }

    public boolean applyCommand(String aCommand) {
        return commandProvider.couldApplyCommand(aCommand);
    }

    public List<String> getAvailableCommandDescriptions() {
        List<String> result = new ArrayList<>();
        for (Command command : commandProvider.getCommands()) {
            result.add(command.getDescription());
        }
        return result;
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

    protected CommandProvider getCommandProvider() {
        return commandProvider;
    }
}
