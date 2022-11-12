package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.ExecutionResult;

public class QuitAction extends AbstractAction {
    @Override
    public ExecutionResult execute() {
        throw new RuntimeException("Bye bye.");
    }
}
