package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.action.GenericCommand;
import com.pdg.adventure.server.api.*;
import com.pdg.adventure.server.support.ActionProvider;
import com.pdg.adventure.server.support.CommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Thing implements Describable, Actionable {

    private final DescriptionProvider descriptionProvider;
    private final ActionProvider actionProvider;
    private final CommandProvider commandProvider;

    private final UUID id;

    public Thing(DescriptionProvider aDescriptionProvider) {
        descriptionProvider = aDescriptionProvider;
        commandProvider = new CommandProvider();
        actionProvider = new ActionProvider();
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

    public boolean applyCommand(GenericCommand aCommand) {
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
    public List<Action> getActions() {
        return  actionProvider.getActions();
    }

    @Override
    public void addAction(Action anAction) {
        actionProvider.addAction(anAction);
    }

    @Override
    public void removeAction(Action anAction) {
        actionProvider.removeAction(anAction);
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
