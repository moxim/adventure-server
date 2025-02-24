package com.pdg.adventure.server.action;

import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.VariableProvider;

public abstract class AbstractVariableAction extends AbstractAction {
    protected final transient VariableProvider variableProvider;

    AbstractVariableAction(VariableProvider aVariableProvider, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        variableProvider = aVariableProvider;
    }

    public VariableProvider getVariableProvider() {
        return variableProvider;
    }
}
