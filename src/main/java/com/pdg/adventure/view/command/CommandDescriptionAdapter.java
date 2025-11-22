package com.pdg.adventure.view.command;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.view.support.ViewSupporter;

public record CommandDescriptionAdapter(CommandDescriptionData commandDescription) implements Describable {

    public CommandDescriptionAdapter(String aCommandSpecification) {
        this(new CommandDescriptionData(aCommandSpecification));
    }

    public String getVerb() {
        return ViewSupporter.getWordText(commandDescription.getVerb());
    }

    @Override
    public String getAdjective() {
        return ViewSupporter.getWordText(commandDescription.getAdjective());
    }

    @Override
    public String getNoun() {
        return ViewSupporter.getWordText(commandDescription.getNoun());
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
