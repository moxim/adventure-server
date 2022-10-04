package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;

public class MessageAction extends AbstractAction {

    private final String message;

    public MessageAction(String aMessage) {
        message = aMessage;
    }

    @Override
    public void execute() {
        Environment.tell(message);
    }
}
