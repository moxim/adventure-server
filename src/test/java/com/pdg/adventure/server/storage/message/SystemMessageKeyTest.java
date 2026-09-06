package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SystemMessageKeyTest {

    @Test
    void catalogHasSixtyFiveEntries() {
        assertThat(SystemMessageKey.values()).hasSize(65);
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
        assertThat(SystemMessageKey.fromId("40")).contains(SystemMessageKey.SM40);
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
}
