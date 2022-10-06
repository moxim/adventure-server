package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.support.Environment;

public class DescribeAction extends AbstractAction {

    private final Describable target;

    public DescribeAction(Describable aTarget) {
        target = aTarget;
    }

    @Override
    public void execute() {
        Environment.tell(target.getLongDescription());
    }
}
