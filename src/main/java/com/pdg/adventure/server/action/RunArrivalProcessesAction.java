package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

/**
 * Fires the adventure's arrival-triggered Processes (Workflow.arrivalProcesses) - appended to the
 * action list of anything that (re)describes the current location without going through
 * MovePlayerAction (e.g. the built-in "describe"/"look" Response), so a PlayerAtCondition-gated
 * arrival Process evaluates against the location actually being described.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RunArrivalProcessesAction extends AbstractAction {
    private final transient GameContext gameContext;

    public RunArrivalProcessesAction(GameContext aGameContext, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.runArrivalProcesses();
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
