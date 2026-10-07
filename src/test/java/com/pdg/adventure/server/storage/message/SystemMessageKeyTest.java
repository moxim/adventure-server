package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SystemMessageKeyTest {

    @Test
    void catalogHasSixtyEightEntries() {
        assertThat(SystemMessageKey.values()).hasSize(79);
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

    @Test
    void saveAndLoadKeys_haveTheDocumentedPlaceholders() {
        assertThat(placeholders(SystemMessageKey.SAVE_DONE)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.SAVE_FULL)).isEqualTo(2);
        assertThat(placeholders(SystemMessageKey.SLOT_INVALID)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.SAVELOAD_UNAVAILABLE)).isZero();
        assertThat(placeholders(SystemMessageKey.LOAD_LIST_HEADER)).isZero();
        assertThat(placeholders(SystemMessageKey.LOAD_NONE)).isZero();
        assertThat(placeholders(SystemMessageKey.LOAD_DONE)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.LOAD_EMPTY_SLOT)).isEqualTo(1);
        assertThat(placeholders(SystemMessageKey.LOAD_CANNOT)).isZero();
        assertThat(placeholders(SystemMessageKey.LOAD_VERSION_NOTE)).isEqualTo(2);
    }

    private static int placeholders(SystemMessageKey aKey) {
        return com.pdg.adventure.server.support.PlaceholderSpec.of(aKey.defaultText()).argumentPositions().size();
    }
}
