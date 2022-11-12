package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class CarriedCondition implements PreCondition {

    private final Item item;

    public CarriedCondition(Item anItem) {
        item = anItem;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (Environment.getPocket().contains(item)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
