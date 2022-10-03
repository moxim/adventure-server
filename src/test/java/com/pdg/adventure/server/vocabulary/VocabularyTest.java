package com.pdg.adventure.server.vocabulary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyTest {
    private final Vocabulary sut = new Vocabulary();

    @Test
    void addSimpleWord() {
        // given
        sut.addWord("take", Word.WordType.VERB);

        // when

        // then
        assertThat(sut.getSynonym("take").getText()).isEqualTo("take");
    }

    @Test
    void addWordForSynonym() {
        // given
        Word take = new Word("take", Word.WordType.VERB);

        sut.addWord(take);

        // when
        sut.addSynonym("get", take);

        // then
        assertThat(sut.getType("take")).isEqualTo(Word.WordType.VERB);
        assertThat(sut.getType("get")).isEqualTo(Word.WordType.VERB);
        assertThat(sut.getSynonym("get")).isEqualTo(take);
    }
}
