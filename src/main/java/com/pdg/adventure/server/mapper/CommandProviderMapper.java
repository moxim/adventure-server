package com.pdg.adventure.server.mapper;

import java.util.Map;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.parser.CommandProvider;

public abstract class CommandProviderMapper {
    private CommandProviderMapper() {
        // don't instantiate me
    }

    public static CommandProvider map(CommandProviderData aData) {
        CommandProvider result = new CommandProvider();
        result.setId(aData.getId());
        for (Map.Entry<CommandDescriptionData, CommandChainData> entry : aData.getAvailableCommands().entrySet()) {
            final CommandDescription description = CommandDescriptionMapper.map(entry.getKey());
            final CommandChain commandChain = CommandChainMapper.map(entry.getValue());
            result.getAvailableCommands().put(description, commandChain);
        }
        return result;
    }

    public static CommandProviderData map(CommandProvider aData) {
        CommandProviderData result = new CommandProviderData();
        result.setId(aData.getId());
        for (Map.Entry<CommandDescription, CommandChain> entry : aData.getAvailableCommands().entrySet()) {
            final CommandDescriptionData descriptionData = CommandDescriptionMapper.map(entry.getKey());
            final CommandChainData chainData = CommandChainMapper.map(entry.getValue());
            result.getAvailableCommands().put(descriptionData, chainData);
        }
        return result;
    }
}
