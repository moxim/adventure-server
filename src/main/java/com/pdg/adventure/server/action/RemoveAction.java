package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RemoveAction extends AbstractAction {
    @Getter
    private final Wearable thing;

    public RemoveAction(Wearable aThing, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        thing = aThing;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult();
        if (thing.isWorn()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
            result.setResultMessage(
                    SystemMessageKey.SM38.defaultText().formatted(thing.getEnrichedBasicDescription()));
            thing.setIsWorn(false);
        } else {
            result.setResultMessage(
                    SystemMessageKey.SM41.defaultText().formatted(thing.getEnrichedBasicDescription()));
        }
        return result;
    }
}
