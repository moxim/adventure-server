package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.condition.PreConditionData;
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
        Command result = new GenericCommand(description);
        result.setId(aCommandData.getId());
        for (ActionData actionData : aCommandData.getActions()) {
            Mapper<ActionData, Action> actionMapper = mapperSupporter.getMapper(actionData.getClass());
            result.addAction(actionMapper.mapToBO(actionData));
        }
        for (PreConditionData condition : aCommandData.getPreConditions()) {
            Mapper<PreConditionData, PreCondition> conditionMapper = mapperSupporter.getMapper(condition.getClass());
            result.addPreCondition(conditionMapper.mapToBO(condition));
        }
        return result;
    }

    // TODO: round-trip preconditions/actions fully. Currently maps actions only (compile-safe analog of the old getAction mapping).
    public CommandData mapToDO(Command aCommand) {
        CommandData result = new CommandData();
        result.setId(aCommand.getId());
        result.setCommandDescription(commandDescriptionMapper.mapToDO(aCommand.getDescription()));
        result.setPreConditions(null);
        List<ActionData> actions = new ArrayList<>();
        for (Action action : aCommand.getActions()) {
            Mapper<ActionData, Action> actionMapper = mapperSupporter.getMapper(action.getClass());
            actions.add(actionMapper.mapToDO(action));
        }
        result.setActions(actions);
        return result;
    }
}
