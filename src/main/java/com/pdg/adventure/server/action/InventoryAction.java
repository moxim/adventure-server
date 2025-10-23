package com.pdg.adventure.server.action;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class InventoryAction extends AbstractAction {

    private final transient Consumer<String> messageConsumer;
    private final transient Supplier<Container> pocket;

    public InventoryAction(Consumer<String> aMessageConsumer, ContainerSupplier aPocket,
                           MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        messageConsumer = aMessageConsumer;
        pocket = aPocket;
    }

    @Override
    public ExecutionResult execute() {
        messageConsumer.accept(messagesHolder.getMessage("-10"));
        messageConsumer.accept(pocket.get().listContents());
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
