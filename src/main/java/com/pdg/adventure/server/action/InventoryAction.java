package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Environment;

public class InventoryAction extends AbstractAction {

    public InventoryAction() {
    }

    @Override
    public ExecutionResult execute() {
        Environment.tell("You carry:");
        Environment.tell(Environment.getPocket().listContents());
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
