package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandDescriptionTest {

    GenericCommandDescription two = new GenericCommandDescription("1", "2");
    GenericCommandDescription anotherTwo = new GenericCommandDescription("1", "2");
    GenericCommandDescription three = new GenericCommandDescription("3");

    @Test
    void compareToLessOK() {
        // given

        // when

        // then
        assertThat(two).isLessThan(three);
    }

    @Test
    void compareToEqualOK() {
        // given

        // when

        // then
        assertThat(two).isEqualByComparingTo(anotherTwo);
    }


    @Test
    void compareToGreaterOK() {
        // given

        // when

        // then
        assertThat(three).isGreaterThan(two);
    }
}
