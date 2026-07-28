package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.model.basic.CommandDescriptionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandProviderData extends BasicData {
    private Map<String, CommandChainData> availableCommands = new HashMap<>();

    // Grouping ("does a command with this trigger already have a chain?") is a search over
    // the chains' own first-command trigger, not a map-key lookup: the map key is each chain's
    // own stable id (CommandChainData.getId(), assigned once on construction), not anything
    // derived from the trigger - so editing a command's verb/adjective/noun later never moves
    // it to a different map entry or orphans its siblings.
    public CommandChainData get(CommandDescriptionData aKey) {
        String targetSpec = aKey.getCommandSpecification();
        for (CommandChainData chain : availableCommands.values()) {
            if (!chain.getCommands().isEmpty()
                    && chain.getCommands().getFirst().getCommandDescription().getCommandSpecification()
                            .equals(targetSpec)) {
                return chain;
            }
        }
        CommandChainData result = new CommandChainData();
        availableCommands.put(result.getId(), result);
        return result;
    }

    public void add(CommandData aCommand) {
        CommandDescriptionData commandDescription = aCommand.getCommandDescription();
        CommandChainData result = get(commandDescription);
        result.getCommands().add(aCommand);
    }

    public Optional<String> findChainIdContaining(CommandData aCommand) {
        for (Map.Entry<String, CommandChainData> entry : availableCommands.entrySet()) {
            if (entry.getValue().getCommands().contains(aCommand)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
}
