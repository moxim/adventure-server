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
        if (commandCanBeExecuted(availableCommandChains, result, aCommand.getVerb())) {
            result = availableCommandChains.getFirst().execute();
            return result;
        }

        return clarifyExecutionOutcome(result);
    }

    private void reduceCommandChains(List<CommandChain> availableCommandChains, CommandDescription aCommand) {
        var iterator = availableCommandChains.iterator();
        while (iterator.hasNext()) {
            CommandChain chain = iterator.next();
            if (chain.getCommands().isEmpty()) {
                iterator.remove();
            } else {
                Command command = chain.getCommands().getFirst();
                if (!VocabularyData.EMPTY_STRING.equals(aCommand.getAdjective())) {
                    if (!command.getDescription().getAdjective().equals(aCommand.getAdjective())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private boolean commandCanBeExecuted(List<CommandChain> availableCommandChains, ExecutionResult result,
                                         String aVerb) {
        if (availableCommandChains.isEmpty()) {
            result.setResultMessage("I don't know how to do that.");
            return false;
        } else if (availableCommandChains.size() > 1) {
            result.setResultMessage(String.format("What do you want to %s?", aVerb));
            return false;
        }
        return true;
    }
}
