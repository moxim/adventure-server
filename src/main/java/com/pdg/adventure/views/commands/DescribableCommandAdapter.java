package com.pdg.adventure.views.commands;

import lombok.Getter;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.basics.CommandDescriptionData;

public class DescribableCommandAdapter implements Describable {
    @Getter
    private final CommandDescriptionData commandDescription;

    public DescribableCommandAdapter(CommandDescriptionData aCommandDescription) {
        commandDescription = aCommandDescription;
    }

    public DescribableCommandAdapter(String aCommandSpecification) {
        commandDescription = new CommandDescriptionData(aCommandSpecification);
    }

    public String getVerb() {
        return commandDescription.getVerb().getText();
    }

    @Override
    public String getAdjective() {
        return commandDescription.getAdjective().getText();
    }

    @Override
    public String getNoun() {
        return commandDescription.getNoun().getText();
    }

    @Override
    public String getBasicDescription() {
        return null;
    }

    @Override
    public String getEnrichedBasicDescription() {
        return null;
    }

    @Override
    public String getShortDescription() {
        return commandDescription.getCommandSpecification();
    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public String getEnrichedShortDescription() {
        return null;
    }

    @Override
    public String getId() {
        return commandDescription.getId();
    }

    @Override
    public void setId(String anId) {
        commandDescription.setId(anId);
    }
}
