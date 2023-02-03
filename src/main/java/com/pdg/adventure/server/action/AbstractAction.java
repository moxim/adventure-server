package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.server.storage.messages.MessagesHolder;

public abstract class AbstractAction extends IdedAction implements Action {
    protected final MessagesHolder messagesHolder;

    public AbstractAction(MessagesHolder aMessagesHolder) {
        messagesHolder = aMessagesHolder;
    }
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
