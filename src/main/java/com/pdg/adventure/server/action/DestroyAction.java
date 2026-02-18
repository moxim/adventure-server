package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class DestroyAction extends AbstractAction {

    private final Containable thing;

    public DestroyAction(Containable aThing, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        thing = aThing;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = thing.getParentContainer().remove(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage(messagesHolder.getMessage("-11").formatted(thing.getShortDescription()));
        }
        return result;
    }
}
