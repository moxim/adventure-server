package com.pdg.adventure.server.action;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class InventoryAction extends AbstractAction {

    private final Consumer messageConsumer;
    private final Supplier<Container> pocket;

    public InventoryAction(Consumer aMessageConsumer, Supplier<Container> aPocket) {
        messageConsumer = aMessageConsumer;
        pocket = aPocket;
    }

    @Override
    public ExecutionResult execute() {
        messageConsumer.accept("You carry:"); // Environment.tell("You carry:");
        messageConsumer.accept(pocket.get().listContents());
//        Environment.tell(pocket.listContents()); //Environment.getPocket()
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
