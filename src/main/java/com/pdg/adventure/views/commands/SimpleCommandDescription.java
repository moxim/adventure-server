package com.pdg.adventure.views.commands;

import lombok.Data;

import com.pdg.adventure.model.basics.CommandDescriptionData;

@Data
class SimpleCommandDescription {
    private String verb;
    private String adjective;
    private String noun;

    public SimpleCommandDescription(String aCommandSpecification) {
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(aCommandSpecification);
        verb = commandDescriptionData.getVerb().getText();
        adjective = commandDescriptionData.getAdjective().getText();
        noun = commandDescriptionData.getNoun().getText();
    }

    public SimpleCommandDescription(String aVerb, String anAdjective, String aNoun) {
        verb = aVerb;
        adjective = anAdjective;
        noun = aNoun;
    }
}
