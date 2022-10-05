package com.pdg.adventure.server.vocabulary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyTest {
    private final Vocabulary sut = new Vocabulary();

    @Test
    void addSimpleWord() {
        // given
        sut.addWord("take", Vocabulary.WordType.VERB);

        // when

        // then
        assertThat(sut.getSynonym("take").getText()).isEqualTo("take");
        assertThat(sut.getSynonym("take").getType()).isEqualTo(Vocabulary.WordType.VERB);
    }

    @Test
    void addWordForSynonym() {
        // given
        sut.addWord("take", Vocabulary.WordType.VERB);

        // when
        sut.addSynonym("get", "take");

        // then
        assertThat(sut.getType("take")).isEqualTo(Vocabulary.WordType.VERB);
        assertThat(sut.getType("get")).isEqualTo(Vocabulary.WordType.VERB);
        assertThat(sut.getSynonym("get").getText()).isEqualTo("take");
    }
}
