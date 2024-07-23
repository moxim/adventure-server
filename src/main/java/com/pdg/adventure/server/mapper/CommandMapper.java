package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.MapperSupporter;
import org.springframework.stereotype.Service;

@Service
public class CommandMapper implements Mapper<CommandData//<SomeAction>
 , Command>  {

    private final MapperSupporter mapperSupporter;

    public CommandMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    public Command mapToBO(CommandData //<SomeAction>
                                   aCommandData) {
        final CommandDescriptionMapper commandDescriptionMapper = mapperSupporter.getMapper(CommandDescriptionMapper.class);
        CommandDescription description = commandDescriptionMapper.mapToBO(aCommandData.getCommandDescription());
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

    public CommandData mapToDO(Command aCommand) {
        CommandData result = new CommandData();
        result.setId(aCommand.getId());
        final CommandDescriptionMapper commandDescriptionMapper = mapperSupporter.getMapper(CommandDescriptionMapper.class);
        result.setCommandDescription(commandDescriptionMapper.mapToDO(aCommand.getDescription()));
        aCommand.getPreconditions();
        result.setPreConditions(null);
        result.setFollowUpActions(null);
        result.setAction(aCommand.getAction());
        return result;
    }
}
