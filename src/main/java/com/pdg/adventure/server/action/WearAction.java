package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class WearAction extends AbstractAction {
    @Getter
    private final Wearable thing;

    public WearAction(Wearable aThing, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        thing = aThing;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult();
        if (thing.isWearable() && !thing.isWorn()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
            result.setResultMessage(SystemMessageKey.SM37.defaultText().formatted(thing.getStrippedBasicDescription()));
            thing.setIsWorn(true);
        } else {
            result.setExecutionState(ExecutionResult.State.FAILURE);
            result.setResultMessage(SystemMessageKey.SM40.defaultText().formatted(thing.getStrippedBasicDescription()));
        }
        return result;
    }
}
