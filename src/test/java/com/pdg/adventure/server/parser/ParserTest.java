package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void getInput() {
        // given
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.addWord("PaRsEr", Vocabulary.WordType.NOUN);
        Parser parser = new Parser(vocabulary);

        // when
        CommandDescription command = parser.handle("hello, I am your trusty parser");

        // then
        assertThat(command.getVerb()).isEmpty();
        assertThat(command.getAdjective()).isEmpty();
        assertThat(command.getNoun()).isEqualTo("parser");
    }
}
