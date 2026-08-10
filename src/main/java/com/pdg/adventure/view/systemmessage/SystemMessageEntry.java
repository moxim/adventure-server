package com.pdg.adventure.view.systemmessage;

import java.time.Instant;

import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/** Grid/dialog read-model joining one adventure's persisted {@link SystemMessageData} row with its catalog metadata. */
record SystemMessageEntry(String id, String defaultText, String sourceLocation, String description, String text,
                          Instant updatedAt) {

    static SystemMessageEntry from(SystemMessageData aData) {
        SystemMessageKey key = SystemMessageKey.fromId(aData.getKey())
                .orElseThrow(() -> new IllegalStateException("Unknown system message key: " + aData.getKey()));
        return new SystemMessageEntry(key.id(), key.defaultText(), key.sourceLocation(), key.description(),
                                      aData.getText(), aData.getUpdatedAt());
    }

    boolean isEdited() {
        return !text.equals(defaultText);
    }
}
