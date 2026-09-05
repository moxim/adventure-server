package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class QuitAction extends AbstractAction {
    public QuitAction(MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
    }

    @Override
    public ExecutionResult execute() {
        throw new QuitException();
    }
}
