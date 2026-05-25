package com.pdg.adventure.server.parser;

import java.util.List;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.VocabularyData;

public class CommandExecutor {

    private final Actionable pocket;
    private final Actionable location;

    public CommandExecutor(Actionable aPocket, Actionable aLocation) {
        pocket = aPocket;
        location = aLocation;
    }

    public static ExecutionResult clarifyExecutionOutcome(ExecutionResult result) {
        if (ExecutionResult.State.FAILURE == result.getExecutionState()) {
            if (VocabularyData.EMPTY_STRING.equals(result.getResultMessage())) {
                result.setResultMessage("You can't do that.");
            }
        } else {
            if (VocabularyData.EMPTY_STRING.equals(result.getResultMessage())) {
                result.setResultMessage("OK.");
            }
        }

        return result;
    }

    public ExecutionResult execute(CommandDescription aCommand) {
        // first look for commands in the pocket
        List<CommandChain> availableCommandChains = pocket.getMatchingCommandChain(aCommand);
        // then add commands in the location itself, the directions and the items in the location
        availableCommandChains.addAll(location.getMatchingCommandChain(aCommand));

        reduceCommandChains(availableCommandChains, aCommand);

        ExecutionResult result = new CommandExecutionResult();
        if (commandCanBeExecuted(availableCommandChains, result, aCommand.getNoun(), aCommand.getVerb())) {
            result = availableCommandChains.getFirst().execute();
            return result;
        }

        return clarifyExecutionOutcome(result);
    }

    private void reduceCommandChains(List<CommandChain> availableCommandChains, CommandDescription aCommand) {
        availableCommandChains.removeIf(c -> c.getCommands().isEmpty());

        // GenericCommandProvider treats EMPTY_STRING as a wildcard, so a chain whose own
        // adjective is empty is a valid match for any input adjective. Drop chains with a
        // non-empty, non-matching adjective. Then, if any exact-adjective match remains,
        // also drop the wildcards so the engine can pick the specific one unambiguously.
        String cmdAdj = aCommand.getAdjective();
        if (VocabularyData.EMPTY_STRING.equals(cmdAdj)) {
            return;
        }
        availableCommandChains.removeIf(c -> {
            String adj = c.getCommands().getFirst().getDescription().getAdjective();
            return !VocabularyData.EMPTY_STRING.equals(adj) && !adj.equals(cmdAdj);
        });
        boolean hasExactMatch = availableCommandChains.stream().anyMatch(c ->
                cmdAdj.equals(c.getCommands().getFirst().getDescription().getAdjective()));
        if (hasExactMatch) {
            availableCommandChains.removeIf(c ->
                    VocabularyData.EMPTY_STRING.equals(c.getCommands().getFirst().getDescription().getAdjective()));
        }
    }

    private boolean commandCanBeExecuted(List<CommandChain> availableCommandChains, ExecutionResult result,
                                         String aNoun, String aVerb) {
        if (availableCommandChains.isEmpty()) {
            result.setResultMessage("I don't know how to do that.");
            return false;
        } else if (availableCommandChains.size() > 1) {
            result.setResultMessage("What do you want to %s?".formatted(aVerb));
            result.setResultMessage("Which %s do you want to %s?".formatted(aNoun, aVerb));
            return false;
        }
        return true;
    }
}
