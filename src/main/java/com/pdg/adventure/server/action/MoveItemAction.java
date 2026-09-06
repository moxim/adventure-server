package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MoveItemAction extends AbstractAction {

    private final Item target;
    private final Container destination;

    public MoveItemAction(Item aTarget, Container aDestination, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        target = aTarget;
        destination = aDestination;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        if (destination.getSize() < destination.getMaxSize()) {
            Container parentContainer = target.getParentContainer();
            if (parentContainer != null) {
                result = parentContainer.remove(target);
            }
            if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
                result = destination.add(target);
                result.setResultMessage(
                        SystemMessageKey.SM56.defaultText().formatted(target.getStrippedBasicDescription(),
                                                   destination.getStrippedBasicDescription()));
            } else {
                result.setExecutionState(ExecutionResult.State.FAILURE);
                result.setResultMessage(SystemMessageKey.SM55.defaultText().formatted(destination.getStrippedBasicDescription()));
            }
        } else {
            result.setExecutionState(ExecutionResult.State.FAILURE);
            result.setResultMessage(SystemMessageKey.SM54.defaultText().formatted(destination.getStrippedBasicDescription()));
        }
        return result;
    }
}
