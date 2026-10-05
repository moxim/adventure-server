package com.pdg.adventure.server.storage.message;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.model.AdventureFont;

public class MessagesHolder {
    // text and font live in one entry so remove/clear can never leave one of them behind
    private record Entry(String text, AdventureFont font) {
    }

    private final Map<String, Entry> messages = new HashMap<>();

    public void addMessage(String anId, String aMessage) {
        addMessage(anId, aMessage, AdventureFont.DEFAULT);
    }

    public void addMessage(String anId, String aMessage, AdventureFont aFont) {
        messages.put(anId, new Entry(aMessage, aFont == null ? AdventureFont.DEFAULT : aFont));
    }

    public String getMessage(String anId) {
        Entry entry = messages.get(anId);
        return entry == null ? null : entry.text();
    }

    /** The font of the message; {@link AdventureFont#DEFAULT} (no override) when it has none or is unknown. */
    public AdventureFont getFont(String anId) {
        Entry entry = messages.get(anId);
        return entry == null ? AdventureFont.DEFAULT : entry.font();
    }

    public void removeMessage(String anId) {
        messages.remove(anId);
    }

    public void clear() {
        messages.clear();
    }
}
