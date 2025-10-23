package com.pdg.adventure.view.command;

import lombok.Getter;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;

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
        return getWordTextSafely(commandDescription.getVerb());
    }

    @Override
    public String getAdjective() {
        return getWordTextSafely(commandDescription.getAdjective());
    }

    @Override
    public String getNoun() {
        return getWordTextSafely(commandDescription.getNoun());
    }

    public String getWordTextSafely(Word aWord) {
        return aWord != null ? aWord.getText() : "";
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
