package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class BreakAction extends AbstractAction {

    public BreakAction(MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
    }

    @Override
    public ExecutionResult execute() {
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isBreak() {
        return true;
    }
}
