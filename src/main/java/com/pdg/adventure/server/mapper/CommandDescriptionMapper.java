package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.basics.CommandDescriptionData;

public abstract class CommandDescriptionMapper {
    private CommandDescriptionMapper() {
        // don't instantiate me
    }

    public static CommandDescriptionData map(CommandDescription aCommandDescription) {
//        CommandDescriptionData result = new CommandDescriptionData();
//        result.setId(aCommandDescription.getId());
//        result.setVerb(aCommandDescription.getVerb());
//        result.setAdjective(aCommandDescription.getAdjective());
//        result.setNoun(aCommandDescription.getNoun());
//        return result;
        return null;
    }

    public static CommandDescription map(CommandDescriptionData aCommandDescriptionData) {
//        CommandDescription result = new GenericCommandDescription(aCommandDescriptionData.getVerb(),
//                                                                  aCommandDescriptionData.getAdjective(),
//                                                                  aCommandDescriptionData.getNoun());
//        result.setId(aCommandDescriptionData.getId());
//        return result;
        return null;
    }
}
