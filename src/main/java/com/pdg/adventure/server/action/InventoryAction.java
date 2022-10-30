package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;

public class InventoryAction extends AbstractAction {

    public InventoryAction() {
    }

    @Override
    public void execute() {
        Environment.tell("You carry:");
        Environment.tell(Environment.getPocket().listContents());
    }
}
