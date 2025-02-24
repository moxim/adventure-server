package com.pdg.adventure.server.action;

import java.util.function.Supplier;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;

public class DescribeAction extends AbstractAction {

    private final transient Supplier<String> target;

    public DescribeAction(Supplier<String> aFunction, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        target = aFunction;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(target.get());
        return result;
    }
}
