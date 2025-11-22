package com.pdg.adventure.server.vocabulary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assumptions.assumeThat;

import com.pdg.adventure.model.Word;

class VocabularyTest {
    private final Vocabulary sut = new Vocabulary();

    {
        sut.createNewWord("take", Word.Type.VERB);
    }

    @Test
    void makeSureTextAndTypeAndSynonymMatch() {
        // given
        assumeThat(sut.findWord("take")).isPresent();

        // when
        final Word.Type takeType = sut.getType("take");
        final Word synonym = sut.findSynonym("take");

        // then
        assertThat(takeType).isEqualTo(Word.Type.VERB);
        assertThat(synonym).isNull();
    }

    @Test
    void createSynonymForNonExistingWordMustFail() {
        // given
        assumeThat(sut.findWord("pick")).isEmpty();

        // when
        assertThatIllegalArgumentException().isThrownBy(() ->
                                                                sut.createSynonym("get", "pick")
        ).withMessageContaining("not present");

        // then
    }

    @Test
    void addSynonymForWordAndFindIt() {
        // given
        assumeThat(sut.findWord("take")).isPresent();
        assumeThat(sut.findWord("get")).isEmpty();

        // when
        Word get = sut.createSynonym("get", "take");

        // then
        assertThat(sut.getType("get")).isEqualTo(sut.getType("take"));
        assertThat(sut.findSynonym("get").getText()).isEqualTo("take");
        assertThat(get.getType()).isEqualTo(sut.getType("take"));
        assertThat(get.getSynonym().getText()).isEqualTo("take");
    }

    @Test
    void addSynonymForWordOfSynonymAndFindIt() {
        // given
        assumeThat(sut.findWord("take")).isPresent();
        assumeThat(sut.findWord("get")).isEmpty();
        sut.createSynonym("pick", "take");

        // when
        Word get = sut.createSynonym("get", "pick");

        // then
        assertThat(get.getType()).isEqualTo(sut.getType("take"));
        assertThat(get.getSynonym().getText()).isEqualTo("take");
    }

}
