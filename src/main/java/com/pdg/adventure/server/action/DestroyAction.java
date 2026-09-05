package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DestroyAction extends AbstractAction {

    @Getter
    private final Containable thing;

    public DestroyAction(Containable aThing, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        thing = aThing;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = thing.getParentContainer().remove(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO: really deliver this message?
            result.setResultMessage(SystemMessageKey.SM57.defaultText().formatted(thing.getStrippedBasicDescription()));
        }
        return result;
    }
}
