package com.pdg.adventure.server.vocabulary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyTest {
    private final Vocabulary sut = new Vocabulary();

    @Test
    void addSimpleWord() {
        // given
        sut.addWord("take", Word.Type.VERB);

        // when

        // then
        assertThat(sut.getSynonym("take").getText()).isEqualTo("take");
        assertThat(sut.getSynonym("take").getType()).isEqualTo(Word.Type.VERB);
    }

    @Test
    void addWordForSynonym() {
        // given
        sut.addWord("take", Word.Type.VERB);

        // when
        sut.addSynonym("get", "take");

        // then
        assertThat(sut.getType("take")).isEqualTo(Word.Type.VERB);
        assertThat(sut.getType("get")).isEqualTo(Word.Type.VERB);
        assertThat(sut.getSynonym("get").getText()).isEqualTo("take");
    }
}
