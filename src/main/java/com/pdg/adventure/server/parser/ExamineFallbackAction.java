package com.pdg.adventure.server.parser;

import java.util.function.Supplier;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.ExecutionResult;

class ExamineFallbackAction implements Action {

    private final transient Supplier<String> description;

    ExamineFallbackAction(Supplier<String> aDescription) {
        description = aDescription;
    }

    @Override
    public ExecutionResult execute() {
        CommandExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(description.get());
        return result;
    }

    @Override
    public String getActionName() {
        return "ExamineFallback";
    }
}
