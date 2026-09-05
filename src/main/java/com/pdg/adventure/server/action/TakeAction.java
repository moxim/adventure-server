package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TakeAction extends AbstractAction {

    @Getter
    private final Item item;
    private final transient ContainerSupplier containerProvider;

    public TakeAction(Item anItem, ContainerSupplier aContainerProvider, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        item = anItem;
        containerProvider = aContainerProvider;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new MoveItemAction(item, containerProvider.get(), messagesHolder).execute();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // I now have the ...
            result.setResultMessage(SystemMessageKey.SM36.defaultText().formatted(item.getStrippedBasicDescription()));
        } else {
            // There isn't one of those here
            result.setResultMessage(SystemMessageKey.SM26.defaultText().formatted(item.getStrippedBasicDescription()));
        }
        return result;
    }
}
