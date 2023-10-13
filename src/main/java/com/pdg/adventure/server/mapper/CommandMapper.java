package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.server.parser.GenericCommand;

public abstract class CommandMapper {
    private CommandMapper() {
        // don't instantiate me
    }

    public static Command mapToBO(CommandData aCommandData) {
        CommandDescription description = CommandDescriptionMapper.map(aCommandData.getCommandDescription());
        Command result = new GenericCommand(description, aCommandData.getAction());
        result.setId(aCommandData.getId());
        for (Action action : aCommandData.getFollowUpActions()) {
            result.addFollowUpAction(action);
        }
        for (PreCondition condition : aCommandData.getPreConditions()) {
            result.addPreCondition(condition);
        }

        return result;
    }

    public static CommandData mapToBO(Command aCommand) {
        CommandData result = new CommandData();
        result.setId(aCommand.getId());
        result.setCommandDescription(CommandDescriptionMapper.map(aCommand.getDescription()));
        aCommand.getPreconditions();
        result.setPreConditions(null);
        result.setFollowUpActions(null);
        result.setAction(aCommand.getAction());
        return result;
    }
}
