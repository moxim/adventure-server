package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.engine.Environment;

public class CarriedCondition implements PreCondition {

    private final Containable item;

    public CarriedCondition(Containable anItem) {
        item = anItem;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (Environment.getPocket().contains(item)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage(String.format("You don't have a %s.", item.getShortDescription()));
        }
        return result;
    }
}
