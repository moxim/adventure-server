package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.tangible.Item;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PresentCondition extends AbstractCondition {
    private final Item thing;

    public PresentCondition(Item aThing) {
        thing = aThing;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (Environment.getCurrentLocation().contains(thing)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage(String.format("There is no %s here.", thing.getNoun()));
        }
        return result;
    }
}
