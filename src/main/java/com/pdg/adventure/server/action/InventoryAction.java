package com.pdg.adventure.server.action;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class InventoryAction extends AbstractAction {

    private final Consumer<String> messageConsumer;
    private final Supplier<Container> pocket;

    public InventoryAction(Consumer<String> aMessageConsumer, Supplier<Container> aPocket) {
        messageConsumer = aMessageConsumer;
        pocket = aPocket;
    }

    @Override
    public ExecutionResult execute() {
        messageConsumer.accept("You carry:");
        messageConsumer.accept(pocket.get().listContents());
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
