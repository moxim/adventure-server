package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * Removes a thing from the container it is in. The player is told only if they would notice: the thing was where
 * they are (see {@link GameContext#playerNoticesChangeIn}). Otherwise the thing is removed all the same, without a
 * message.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DestroyAction extends AbstractAction {

    @Getter
    private final Containable thing;
    private final transient GameContext gameContext;

    public DestroyAction(Containable aThing, GameContext aGameContext) {
        thing = aThing;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult();
        final Container parentContainer = thing.getParentContainer();
        if (parentContainer != null) {
            // asked before the removal: a removed thing has no container to be noticed in any more
            boolean noticed = gameContext.playerNoticesChangeIn(parentContainer);
            result = parentContainer.remove(thing);
            if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
                thing.setParentContainer(null);
                if (noticed) {
                    result.setResultMessage(SystemMessageKey.SM57.defaultText().formatted(thing.getStrippedBasicDescription()));
                }
            }
        }
        return result;
    }
}
