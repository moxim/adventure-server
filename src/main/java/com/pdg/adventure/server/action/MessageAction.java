package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.engine.Environment;

public class MessageAction extends AbstractAction {

    private final String message;

    public MessageAction(String aMessage) {
        message = aMessage;
    }

    @Override
    public ExecutionResult execute() {
        // TODO
        //  shouldn't we put the message into the result?
        Environment.tell(message);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
