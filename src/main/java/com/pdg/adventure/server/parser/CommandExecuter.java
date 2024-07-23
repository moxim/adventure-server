package com.pdg.adventure.server.parser;

import com.pdg.adventure.api.Actionable;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;

import java.util.List;

public class CommandExecuter {

    private final Actionable pocket;
    private final Actionable location;

    public CommandExecuter(Actionable aPocket, Actionable aLocation) {
        pocket = aPocket;
        location = aLocation;
    }

    public ExecutionResult execute(CommandDescription aCommand) {
        List<CommandChain> availableCommandChains = pocket.getMatchingCommandChain(aCommand);
        availableCommandChains.addAll(location.getMatchingCommandChain(aCommand));

        ExecutionResult result = new CommandExecutionResult();
        if (availableCommandChains.isEmpty()) {
            result.setResultMessage("I don't know how to do that.");
        } else if (availableCommandChains.size() > 1) {
            result.setResultMessage(String.format("What do you want to %s?", aCommand.getVerb()));
        } else {
            result = availableCommandChains.get(0).execute();
        }

        if (result.getExecutionState()== ExecutionResult.State.FAILURE) {
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


}
