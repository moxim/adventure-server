package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class DropAction extends AbstractAction {

    private final Item item;

    public DropAction(Item anItem) {
        item = anItem;
    }

    @Override
    public void execute() {
        new MoveItemAction(item, Environment.getCurrentLocation().getContainer()).execute();
    }
}
