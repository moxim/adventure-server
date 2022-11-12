package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class DescribeAction extends AbstractAction {

    private final Describable target;

    public DescribeAction(Describable aTarget) {
        target = aTarget;
    }

    @Override
    public ExecutionResult execute() {
        if (target instanceof Location location) { // TODO: get rid of this ugly cast
            location.setHasBeenVisited(false);
        }
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(target.getLongDescription());
        if (target instanceof Location location) {
            location.setHasBeenVisited(true);
        }
        return result;
    }
}
