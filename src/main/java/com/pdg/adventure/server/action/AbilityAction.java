package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Sets what the player can carry: the most weight and the most objects (the pocket's max size). Takes effect on the
 * next take (AutoTake, Take); nothing the player already carries is dropped. Tells nothing and always succeeds; a
 * missing value leaves that limit as it is.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AbilityAction extends AbstractAction {

    private final Integer maxWeight;
    private final Integer maxItems;
    private final transient GameContext gameContext;

    public AbilityAction(Integer aMaxWeight, Integer aMaxItems, GameContext aGameContext) {
        maxWeight = aMaxWeight;
        maxItems = aMaxItems;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        Container pocket = gameContext.getPocket();
        if (pocket != null) {
            if (maxWeight != null) {
                pocket.setMaxWeight(maxWeight);
            }
            if (maxItems != null) {
                pocket.setMaxSize(maxItems);
            }
        }
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
