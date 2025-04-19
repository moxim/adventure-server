package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.model.basics.CommandDescriptionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandProviderData extends BasicData {
    private Map<String, CommandChainData> availableCommands = new HashMap<>();

    public CommandChainData get(CommandDescriptionData aKey) {
        CommandChainData result = availableCommands.getOrDefault(aKey.getCommandSpecification(), new CommandChainData());
        availableCommands.put(aKey.getCommandSpecification(), result);
        return result;
    }

    public void add(CommandData aCommand) {
        CommandDescriptionData commandDescription = aCommand.getCommandDescription();
        CommandChainData result = get(commandDescription);
        result.getCommands().add(aCommand);
    }
}
