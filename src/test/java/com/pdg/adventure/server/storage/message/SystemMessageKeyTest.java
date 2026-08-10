package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.SystemMessageData;

class SystemMessageKeyTest {

    @Test
    void catalogHasThirtySixEntries() {
        assertThat(SystemMessageKey.values()).hasSize(36);
    }

    @Test
    void allKeysHaveUniqueIds() {
        Set<String> ids = new HashSet<>();
        for (SystemMessageKey key : SystemMessageKey.values()) {
            assertThat(ids.add(key.id())).as("duplicate id: " + key.id()).isTrue();
        }
    }

    @Test
    void fromId_findsExistingKey() {
        assertThat(SystemMessageKey.fromId("-6")).contains(SystemMessageKey.CANNOT_WEAR);
    }

    @Test
    void fromId_returnsEmptyForUnknownId() {
        assertThat(SystemMessageKey.fromId("does-not-exist")).isEmpty();
    }

    @Test
    void everyKeyHasNonBlankSourceAndDescription() {
        for (SystemMessageKey key : SystemMessageKey.values()) {
            assertThat(key.sourceLocation()).as(key.name()).isNotBlank();
            assertThat(key.description()).as(key.name()).isNotBlank();
            assertThat(key.defaultText()).as(key.name()).isNotBlank();
        }
    }

    @Test
    void descriptiveKeys_useEnumNameAsId() {
        assertThat(SystemMessageKey.HELP_TEXT.id()).isEqualTo("HELP_TEXT");
    }

    @Test
    void seedMissingInto_populatesAllKeysIntoAnEmptyMap() {
        Map<String, SystemMessageData> systemMessages = new HashMap<>();

        SystemMessageKey.seedMissingInto(systemMessages, "adventure-1");

        assertThat(systemMessages).hasSize(36);
        SystemMessageData wear = systemMessages.get(SystemMessageKey.CANNOT_WEAR.id());
        assertThat(wear.getAdventureId()).isEqualTo("adventure-1");
        assertThat(wear.getKey()).isEqualTo(SystemMessageKey.CANNOT_WEAR.id());
        assertThat(wear.getText()).isEqualTo(SystemMessageKey.CANNOT_WEAR.defaultText());
    }

    @Test
    void seedMissingInto_neverOverwritesAnAlreadyPresentEntry() {
        Map<String, SystemMessageData> systemMessages = new HashMap<>();
        SystemMessageData editedWearMessage = new SystemMessageData("adventure-1", SystemMessageKey.CANNOT_WEAR.id(),
                                                                    "Du kannst %s nicht tragen.");
        systemMessages.put(SystemMessageKey.CANNOT_WEAR.id(), editedWearMessage);

        SystemMessageKey.seedMissingInto(systemMessages, "adventure-1");

        assertThat(systemMessages.get(SystemMessageKey.CANNOT_WEAR.id())).isSameAs(editedWearMessage);
        assertThat(systemMessages).hasSize(36);
    }
}
