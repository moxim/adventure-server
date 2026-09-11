package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.QuitException;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class QuitAction extends AbstractAction {
    public QuitAction() {
    }

    @Override
    public ExecutionResult execute() {
        throw new QuitException();
    }
}
