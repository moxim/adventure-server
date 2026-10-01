package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyDataWildcardNounTest {

    @Test
    void ensureWildcardNoun_createsTheWordOnce() {
        VocabularyData vocabulary = new VocabularyData();

        Word first = vocabulary.ensureWildcardNoun();
        Word second = vocabulary.ensureWildcardNoun();

        assertThat(first.getText()).isEqualTo(VocabularyData.WILDCARD_NOUN);
        assertThat(first.getType()).isEqualTo(Word.Type.NOUN);
        assertThat(second).isSameAs(first);
        assertThat(vocabulary.getWords()).hasSize(1);
    }

    @Test
    void ensureWildcardNoun_leavesAnExistingSameTextWordUntouched() {
        VocabularyData vocabulary = new VocabularyData();
        Word existing = vocabulary.createWord(VocabularyData.WILDCARD_NOUN, Word.Type.VERB);

        assertThat(vocabulary.ensureWildcardNoun()).isSameAs(existing);
        assertThat(existing.getType()).isEqualTo(Word.Type.VERB);
    }
}
