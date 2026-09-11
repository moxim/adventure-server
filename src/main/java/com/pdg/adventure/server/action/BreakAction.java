package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BreakAction extends AbstractAction {

    public BreakAction() {
    }

    @Override
    public ExecutionResult execute() {
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isBreak() {
        return true;
    }
}
