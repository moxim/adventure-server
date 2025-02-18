package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.pdg.adventure.model.Word;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void getInput() {
        // given
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("PaRsEr", Word.Type.NOUN);
        Parser parser = new Parser(vocabulary);

        // when
        GenericCommandDescription command = parser.handle("hello, parser I am your trusty parser");

        // then
        assertThat(command.getVerb()).isEmpty();
        assertThat(command.getAdjective()).isEmpty();
        assertThat(command.getNoun()).isEqualTo("parser");
    }
}
