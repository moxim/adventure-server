package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.DatedData;

/**
 * A built-in, engine-level message available for translation/wording edits, scoped to a single
 * adventure - different adventures can run in different languages, so the catalog is per-adventure
 * rather than global. Unlike {@link MessageData} (also per-adventure but author-authored,
 * creatable/deletable), this catalog is fixed: entries can only be edited, never created or
 * deleted. Default text, source location and translator description live in {@link
 * com.pdg.adventure.server.storage.message.SystemMessageKey} - this stores only the mutable,
 * editable text for one key, embedded in {@link AdventureData#getSystemMessages()} - not an
 * independent, top-level Mongo document.
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SystemMessageData extends DatedData {

    private String adventureId;
    private String key;
    private String text;

    public SystemMessageData() {
    }

    public SystemMessageData(String anAdventureId, String aKey, String aText) {
        adventureId = anAdventureId;
        key = aKey;
        text = aText;
    }
}
