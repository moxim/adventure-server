package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class RemoveAction extends AbstractAction {
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
            thing.setIsWorn(false);
        } else {
            result.setResultMessage(String.format(messagesHolder.getMessage("-7"), thing.getEnrichedBasicDescription()));
        }
        return result;
    }
}
