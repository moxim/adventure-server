package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.QuitException;

public class QuitAction extends AbstractAction {
    @Override
    public ExecutionResult execute() {
        throw new QuitException();
    }
}
