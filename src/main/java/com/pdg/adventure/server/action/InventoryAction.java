package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;

public class InventoryAction extends AbstractAction {

    public InventoryAction() {
    }

    @Override
    public void execute() {
        Environment.showContents(Environment.getPocket(), "You carry:");
    }
}
