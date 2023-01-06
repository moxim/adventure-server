package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

import java.util.function.Supplier;

public class DescribeAction extends AbstractAction {

    private final Supplier<String> target;

    public DescribeAction(Supplier<String> aFunction) {
        target = aFunction;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(target.get());
        return result;
    }
}
