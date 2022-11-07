package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.Environment;

public class DescribeAction extends AbstractAction {

    private final Describable target;

    public DescribeAction(Describable aTarget) {
        target = aTarget;
    }

    @Override
    public void execute() {
        if (target instanceof Location location) { // TODO: get rid of this ugly cast
            location.setHasBeenVisited(false);
        }
        Environment.show(target);
        if (target instanceof Location location) {
            location.setHasBeenVisited(true);
        }
    }
}
