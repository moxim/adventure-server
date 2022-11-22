package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

public class MessageAction extends AbstractAction {

    private final String message;

    public MessageAction(String aMessage) {
        message = aMessage;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(message);
        return result;
    }
}
