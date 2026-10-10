package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SystemMessageKeyTest {

    @Test
    void catalogHasSeventyNineEntries() {
        assertThat(SystemMessageKey.values()).hasSize(78);
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
    void saveAndLoadKeys_haveTheDocumentedPlaceholders() {
        assertThat(placeholders(SystemMessageKey.SM68)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.SM69)).isEqualTo(2);
        assertThat(placeholders(SystemMessageKey.SM70)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.SM71)).isZero();
        assertThat(placeholders(SystemMessageKey.SM72)).isZero();
        assertThat(placeholders(SystemMessageKey.SM73)).isZero();
        assertThat(placeholders(SystemMessageKey.SM74)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.SM75)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.SM76)).isZero();
        assertThat(placeholders(SystemMessageKey.SM77)).isEqualTo(2);
    }

    private static int placeholders(SystemMessageKey aKey) {
        return com.pdg.adventure.server.support.PlaceholderSpec.of(aKey.defaultText()).argumentPositions().size();
    }
}
