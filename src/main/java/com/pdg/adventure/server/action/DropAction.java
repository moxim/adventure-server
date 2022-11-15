package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.tangible.Item;

public class DropAction extends AbstractAction {

    private final Item item;

    public DropAction(Item anItem) {
        item = anItem;
    }

    @Override
    public ExecutionResult execute() {
        return new MoveItemAction(item, Environment.getCurrentLocation().getContainer()).execute();
    }
}
