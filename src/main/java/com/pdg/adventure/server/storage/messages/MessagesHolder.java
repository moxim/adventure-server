package com.pdg.adventure.server.storage.messages;

import java.util.HashMap;
import java.util.Map;

public class MessagesHolder {
    private Map<String, String> messages = new HashMap<>();

    public void addMessage(String anId, String aMessage) {
        messages.put(anId, aMessage);
    }

    public String getMessage(String anId) {
        return messages.get(anId);
    }

    public void removeMessage(String anId) {
        messages.remove(anId);
    }
}
