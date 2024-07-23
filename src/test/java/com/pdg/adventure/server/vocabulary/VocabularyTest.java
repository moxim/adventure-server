package com.pdg.adventure.server.vocabulary;

import com.pdg.adventure.model.Word;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyTest {
    private final Vocabulary sut = new Vocabulary();

    @Test
    void addSimpleWord() {
        // given
        sut.addNewWord("take", Word.Type.VERB);

        // when

        // then
        assertThat(sut.findWord("take").isPresent());
        assertThat(sut.findWord("get").isEmpty());
        assertThat(sut.getType("take")).isEqualTo(Word.Type.VERB);
        assertThat(sut.getSynonym("take")).isNull();
    }

    @Test
    void addWordForSynonym() {
        // given
        sut.addNewWord("take", Word.Type.VERB);

        // when
        sut.addSynonym("get", "take");

        // then
        assertThat(sut.getType("take")).isEqualTo(Word.Type.VERB);
        assertThat(sut.getType("get")).isEqualTo(Word.Type.VERB);
        assertThat(sut.getSynonym("get").getText()).isEqualTo("take");
    }
}
