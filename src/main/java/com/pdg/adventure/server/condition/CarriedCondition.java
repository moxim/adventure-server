package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.pdg.adventure.server.storage.message.SystemMessageKey.SM28;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.tangible.Item;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CarriedCondition extends AbstractCondition {

    @Getter
    private final Item item;
    private final transient GameContext gameContext;

    public CarriedCondition(Item anItem, GameContext aGameContext) {
        item = anItem;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (gameContext.getPocket().contains(item)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage(SM28.defaultText());
        }
        return result;
    }
}
