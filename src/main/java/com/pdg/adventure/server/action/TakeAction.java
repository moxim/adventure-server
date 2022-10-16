package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class TakeAction extends AbstractAction {

    private final Item item;

    public TakeAction(Item anItem) {
        item = anItem;
    }

    @Override
    public void execute() {
        new MoveItemAction(item, Environment.getPocket()).execute();
    }
}
