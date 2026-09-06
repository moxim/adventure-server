package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
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
