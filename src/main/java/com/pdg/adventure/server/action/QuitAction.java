package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class QuitAction extends AbstractAction {
    public QuitAction(MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
    }

    @Override
    public ExecutionResult execute() {
        throw new QuitException();
    }
}
