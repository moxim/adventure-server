package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.function.Supplier;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class InventoryAction extends AbstractAction {

    private final transient Supplier<Container> pocket;

    public InventoryAction(ContainerSupplier aPocket) {
        pocket = aPocket;
    }

    @Override
    public ExecutionResult execute() {
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS,
                SystemMessageKey.SM9.defaultText() + System.lineSeparator() + pocket.get().listContents());
    }
}
