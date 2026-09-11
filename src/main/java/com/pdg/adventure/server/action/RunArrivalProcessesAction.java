package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Fires the adventure's arrival-triggered Processes (Workflow.arrivalProcesses) - appended to the
 * action list of anything that (re)describes the current location without going through
 * MovePlayerAction (e.g. the built-in "describe"/"look" Response), so a PlayerAtCondition-gated
 * arrival Process evaluates against the location actually being described.
 *
 * Known limitation: this only fires via the one built-in "describe"/"look" Response object
 * CommandFactory registers. It is silently bypassed on the explicit-look path - movement-
 * triggered firing via MovePlayerAction is unaffected in all three cases below - whenever:
 * an author adds their own Response with verb "describe" (replaces the built-in by key);
 * a location/pocket command matches "describe"/"look" (wins over the built-in Response before
 * it's ever consulted); or an author attaches a DescribeAction to any command via
 * DescribeActionEditor. See docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md's
 * final-review notes for the full discussion.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RunArrivalProcessesAction extends AbstractAction {
    private final transient GameContext gameContext;

    public RunArrivalProcessesAction(GameContext aGameContext) {
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.runArrivalProcesses();
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
