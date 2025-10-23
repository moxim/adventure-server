package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public abstract class AbstractAction extends IdedAction implements Action {
    protected final transient MessagesHolder messagesHolder;

    protected AbstractAction(MessagesHolder aMessagesHolder) {
        messagesHolder = aMessagesHolder;
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getActionName();
    }
}
