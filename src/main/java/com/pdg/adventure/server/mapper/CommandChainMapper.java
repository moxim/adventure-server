package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.server.parser.GenericCommandChain;
import com.pdg.adventure.server.support.MapperSupporter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommandChainMapper implements Mapper<CommandChainData, CommandChain> {

    private final Mapper<CommandData, Command> commandMapper;

    public CommandChainMapper(MapperSupporter aMapperSupporter) {
        commandMapper = aMapperSupporter.getMapper(CommandData.class);
    }

    public CommandChain mapToBO(CommandChainData aData) {
        CommandChain result = new GenericCommandChain();
        result.setId(aData.getId());
        for (CommandData commandData : aData.getCommands()) {
            final Command command = commandMapper.mapToBO(commandData);
            result.addCommand(command);
        }
        return result;
    }

    public CommandChainData mapToDO(CommandChain aData) {
        CommandChainData result = new CommandChainData();
        result.setId(aData.getId());
        List<CommandData> commandList = new ArrayList<>(aData.getCommands().size());
        for (Command command : aData.getCommands()) {
            final CommandData commandData = commandMapper.mapToDO(command);
            commandList.add(commandData);
        }
        result.setCommands(commandList);
        return result;
    }
}
