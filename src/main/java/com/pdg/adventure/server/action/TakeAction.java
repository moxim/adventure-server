package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.tangible.Item;

public class TakeAction extends AbstractAction {

    private final Item item;

    public TakeAction(Item anItem) {
        item = anItem;
    }

    @Override
    public ExecutionResult execute() {
        return new MoveItemAction(item, Environment.getPocket()).execute();
    }
}
