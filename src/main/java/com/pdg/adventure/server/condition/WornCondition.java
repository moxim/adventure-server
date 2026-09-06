package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class WornCondition extends AbstractCondition {

    @Getter
    private final Wearable thing;

    public WornCondition(Wearable aThing) {
        thing = aThing;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (thing.isWorn()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage(SystemMessageKey.SM50.defaultText().formatted(thing.getEnrichedBasicDescription()));
        }
        return result;
    }
}
