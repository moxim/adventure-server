package com.pdg.adventure.server.action;

import java.util.function.Supplier;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.tangible.Item;

public class DropAction extends AbstractAction {

    private final Item item;
    private final Supplier<Container> containerProvider;

    public DropAction(Item anItem, Supplier<Container> aContainerProvider, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        item = anItem;
        containerProvider = aContainerProvider;
    }

    @Override
    public ExecutionResult execute() {
        return new MoveItemAction(item, containerProvider.get(), messagesHolder).execute();
    }
}
