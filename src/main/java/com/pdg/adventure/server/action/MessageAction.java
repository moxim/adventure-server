package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class MessageAction extends AbstractAction {

    private final String message;

    public MessageAction(String aMessage, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        message = aMessage;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(message);
        return result;
    }
}
