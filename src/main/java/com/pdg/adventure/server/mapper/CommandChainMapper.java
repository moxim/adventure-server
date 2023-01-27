package com.pdg.adventure.server.mapper;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.server.parser.GenericCommandChain;

public abstract class CommandChainMapper {
    private CommandChainMapper() {
        // don't instantiate me
    }

    public static CommandChain map(CommandChainData aData) {
        CommandChain result = new GenericCommandChain();
        result.setId(aData.getId());
        for (CommandData commandData : aData.getCommands()) {
            final Command command = CommandMapper.map(commandData);
            result.addCommand(command);
        }
        return result;
    }

    public static CommandChainData map(CommandChain aData) {
        CommandChainData result = new CommandChainData();
        result.setId(aData.getId());
        List<CommandData> commandList = new ArrayList<>(aData.getCommands().size());
        for (Command command : aData.getCommands()) {
            final CommandData commandData = CommandMapper.map(command);
            commandList.add(commandData);
        }
        result.setCommands(commandList);
        return result;
    }
}
