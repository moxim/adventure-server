package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class InventoryAction extends AbstractAction {

    private final Consumer<String> messageConsumer;
    private final Supplier<Container> pocket;

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
