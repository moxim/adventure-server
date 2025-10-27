package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import java.util.List;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Command mapping with dynamic action resolution")
public class CommandMapper implements Mapper<CommandData, Command> {

    private final MapperSupporter mapperSupporter;
    private final Mapper<CommandDescriptionData, CommandDescription> commandDescriptionMapper;

    public CommandMapper(MapperSupporter aMapperSupporter,
                         CommandDescriptionMapper aCommandDescriptionMapper) {
        mapperSupporter = aMapperSupporter;
        commandDescriptionMapper = aCommandDescriptionMapper;
        mapperSupporter.registerMapper(CommandData.class, Command.class, this);
    }

    public Command mapToBO(CommandData aCommandData) {
        CommandDescription description = commandDescriptionMapper.mapToBO(aCommandData.getCommandDescription());
        final ActionData mainActionData = aCommandData.getAction();
        Action actionBO = null;
        Mapper<ActionData, Action> actionMapper = null;
        if (mainActionData != null) {
            actionMapper = mapperSupporter.getMapper(mainActionData.getClass());
            actionBO = actionMapper.mapToBO(mainActionData);
        }
        Command result = new GenericCommand(description, actionBO);
        result.setId(aCommandData.getId());
        for (ActionData subActionData : aCommandData.getFollowUpActions()) {
            if (subActionData != null) {
                actionMapper = mapperSupporter.getMapper(subActionData.getClass());
                result.addFollowUpAction(actionMapper.mapToBO(subActionData));
            }
        }
        for (PreCondition condition : aCommandData.getPreConditions()) {
            result.addPreCondition(condition);
        }
        return result;
    }

    // TODO: Implement this method
    public CommandData mapToDO(Command aCommand) {
        CommandData result = new CommandData();
        result.setId(aCommand.getId());
        result.setCommandDescription(commandDescriptionMapper.mapToDO(aCommand.getDescription()));
        final List<PreCondition> preconditions = aCommand.getPreconditions();
        result.setPreConditions(null);
        result.setFollowUpActions(null);
        Mapper<ActionData, Action> actionMapper = mapperSupporter.getMapper(aCommand.getAction().getClass());
        result.setAction(actionMapper.mapToDO(aCommand.getAction()));
        return result;
    }
}
