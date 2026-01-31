package com.pdg.adventure.server.action;

import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.tangible.Item;

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
        return new MoveItemAction(item, containerProvider.get(), messagesHolder).execute();
    }
}
