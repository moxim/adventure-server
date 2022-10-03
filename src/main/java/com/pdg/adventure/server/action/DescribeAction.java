package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class DescribeAction extends AbstractAction {

    private final Item target;

    public DescribeAction(Item aTarget) {
        target = aTarget;
    }

    @Override
    public void execute() {
        Environment.tell(target.getLongDescription());
    }
}
