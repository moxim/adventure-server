package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@DependsOn({ // TODO: list all action mappers here
             "movePlayerActionMapper",
             "setVariableActionMapper",
             "wearActionMapper",
             "commandDescriptionMapper",
             "mapperSupporter"})
public class CommandMapper implements Mapper<CommandData, Command> {

    private final MapperSupporter mapperSupporter;
    private Mapper<ActionData, Action> actionMapper;
    private Mapper<CommandDescriptionData, CommandDescription> commandDescriptionMapper;


    public CommandMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
        commandDescriptionMapper = mapperSupporter.getMapper(CommandDescriptionData.class);
        mapperSupporter.registerMapper(CommandData.class, Command.class, this);
    }

    public Command mapToBO(CommandData aCommandData) {
        CommandDescription description = commandDescriptionMapper.mapToBO(aCommandData.getCommandDescription());
        final ActionData mainActionData = aCommandData.getAction();
        Action actionBO = null;
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
        result.setAction(actionMapper.mapToDO(aCommand.getAction()));
        return result;
    }
}
