package com.pdg.adventure.view.command;

import lombok.Getter;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;

@Getter
public class CommandViewModel {
    private final CommandDescriptionData data;
    private Word verb;
    private Word adjective;
    private Word noun;
    private String specification;

    public CommandViewModel(CommandDescriptionData aCommandDescriptionData) {
        data = aCommandDescriptionData;
        this.verb = aCommandDescriptionData.getVerb();
        this.adjective = aCommandDescriptionData.getAdjective();
        this.noun = aCommandDescriptionData.getNoun();
        this.specification = aCommandDescriptionData.getCommandSpecification();
    }

    public void setVerb(final Word aVerb) {
        verb = aVerb;
        data.setVerb(aVerb);
    }

    public void setAdjective(final Word anAdjective) {
        adjective = anAdjective;
        data.setAdjective(anAdjective);
    }

    public void setNoun(final Word aNoun) {
        noun = aNoun;
        data.setNoun(aNoun);
    }

    public void setSpecification(final String aSpecification) {
        specification = aSpecification;
        data.setCommandSpecification(aSpecification);
    }
}
