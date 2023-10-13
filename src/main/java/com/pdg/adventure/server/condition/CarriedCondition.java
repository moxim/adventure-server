package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.tangible.Item;

public class CarriedCondition extends AbstractCondition {

    private final Item item;

    public CarriedCondition(Item anItem) {
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
