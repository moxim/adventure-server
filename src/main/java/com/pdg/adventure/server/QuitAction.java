package com.pdg.adventure.server;

import com.pdg.adventure.server.action.AbstractAction;

public class QuitAction extends AbstractAction {
    @Override
    public void execute() {
        throw new RuntimeException("Bye bye.");
    }
}
