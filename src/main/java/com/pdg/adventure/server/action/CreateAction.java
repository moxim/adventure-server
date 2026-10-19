package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.function.Supplier;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * Puts a thing into a container, taking it out of the one it was in. The player is told only if they would notice: the
 * thing appears where they are (see {@link GameContext#playerNoticesChangeIn}). Otherwise the thing is placed all the
 * same, without a message.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CreateAction extends AbstractAction {

    private final Containable thing;
    private final transient Supplier<Container> containerProvider;
    private final transient GameContext gameContext;

    public CreateAction(Containable aThing, Supplier<Container> aContainerProvider, GameContext aGameContext) {
        thing = aThing;
        containerProvider = aContainerProvider;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        // remove the thing from its current container if it has one, to avoid duplicates
        new DestroyAction(thing, gameContext).execute();

        Container container = containerProvider.get();
        ExecutionResult result = container.add(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            if (gameContext.playerNoticesChangeIn(container)) {
                result.setResultMessage(SystemMessageKey.SM58.defaultText().formatted(
                        capitalizeFirstLetter(thing.getEnrichedShortDescription()),
                        container.getStrippedBasicDescription())
                );
            }
            thing.setParentContainer(container);
        }
        return result;
    }
}
