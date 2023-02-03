package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.storage.vocabulary.Vocabulary;
import com.pdg.adventure.server.storage.vocabulary.Word;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void getInput() {
        // given
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.addWord("PaRsEr", Word.Type.NOUN);
        Parser parser = new Parser(vocabulary);

        // when
        GenericCommandDescription command = parser.handle("hello, I am your trusty parser");

        // then
        assertThat(command.getVerb()).isEmpty();
        assertThat(command.getAdjective()).isEmpty();
        assertThat(command.getNoun()).isEqualTo("parser");
    }
}
