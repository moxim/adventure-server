package com.pdg.adventure.server.tangible;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.pdg.adventure.api.*;
import com.pdg.adventure.server.parser.CommandHandler;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;

public class Thing implements Actionable, HasLight {

    private String id;
    @Setter
    private DescriptionProvider descriptionProvider;
    private final transient CommandHandler commandHandler;
    // 0 (no light emitted) by default: most Things (a key, a sword) don't glow. A subclass that
    // should be lit by default (Location, representing ambient room light) sets its own default.
    private int lumen;

    public Thing(DescriptionProvider aDescriptionProvider) {
        commandHandler = new CommandHandler();
        descriptionProvider = aDescriptionProvider;
        id = UUID.randomUUID().toString();
    }

    @Override
    public void setLight(int aLumenValue) {
        lumen = aLumenValue;
    }

    @Override
    public int getLight() {
        return lumen;
    }

    public void setExamineFallback(String aVerb, Supplier<String> aDescription) {
        commandHandler.setExamineFallback(aVerb, aDescription, this::getNoun, this::getAdjective);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
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
    public String getStrippedBasicDescription() {
        return descriptionProvider.getStrippedBasicDescription();
    }

    @Override
    public String getEnrichedBasicDescription() {
        return descriptionProvider.getEnrichedBasicDescription();
    }

    @Override
    public String getShortDescription() {
        return descriptionProvider.getShortDescription();
    }

    @Override
    public String getStrippedShortDescription() {
        return descriptionProvider.getStrippedShortDescription();
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
        return descriptionProvider.getEnrichedShortDescription(getStrippedShortDescription());
    }

    public void setLongDescription(String aLongDescription) {
        descriptionProvider.setLongDescription(aLongDescription);
    }

    @Override
    public ExecutionResult applyCommand(CommandDescription aCommand) {
        return commandHandler.applyCommand(aCommand);
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
    public List<Command> getCommands() {
        return new ArrayList<>(commandHandler.getCommands());
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
    public GenericCommandProvider getCommandProvider() {
        return commandHandler.getCommandProvider();
    }

    public void setCommandProvider(GenericCommandProvider aCommandProvider) {
        commandHandler.setCommandProvider(aCommandProvider);
    }

    public DescriptionProvider getDescriptionProvider() {
        return descriptionProvider;
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
        return "Thing{" +
               "descriptionProvider=" + descriptionProvider +
               ", commandProvider=" + commandHandler.getCommandProvider() +
               ", id='" + id + '\'' +
               '}';
    }
}
