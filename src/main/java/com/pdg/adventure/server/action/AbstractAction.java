package com.pdg.adventure.server.action;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object aO) {
        if (aO == null || getClass() != aO.getClass()) return false;
        if (!super.equals(aO)) return false;

        AbstractAction that = (AbstractAction) aO;
        return Objects.equals(getActionName(), that.getActionName());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(getActionName());
        return result;
    }
}
