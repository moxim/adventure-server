package com.pdg.adventure.view.systemmessage;

import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * Grid/dialog read-model for one catalog key in one adventure. Storage is sparse - anOverride is
 * null when the adventure has never customized this key, in which case text falls back to the
 * key's own default.
 */
record SystemMessageEntry(String id, String defaultText, String sourceLocation, String description, String text) {

    static SystemMessageEntry forKey(SystemMessageKey aKey, SystemMessageData anOverride) {
        String text = anOverride != null ? anOverride.getText() : aKey.defaultText();
        return new SystemMessageEntry(aKey.id(), aKey.defaultText(), aKey.sourceLocation(), aKey.description(), text);
    }
}
