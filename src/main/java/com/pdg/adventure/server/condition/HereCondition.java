package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.tangible.Item;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class HereCondition extends AbstractCondition {

    @Getter
    private final Item thing;
    private final GameContext gameContext;

    public HereCondition(Item aThing, GameContext aGameContext) {
        thing = aThing;
        gameContext = aGameContext;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (gameContext.getCurrentLocation().contains(thing)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage("There is no %s here.".formatted(thing.getNoun()));
        }
        return result;
    }
}
