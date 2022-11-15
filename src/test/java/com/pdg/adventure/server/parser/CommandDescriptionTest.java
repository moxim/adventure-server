package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandDescriptionTest {

    GenericCommandDescription sut = new GenericCommandDescription("1", "2");
    GenericCommandDescription one = new GenericCommandDescription("1", "2");
    GenericCommandDescription three = new GenericCommandDescription("3");

    @Test
    void compareToLessOK() {
        // given

        // when

        // then
        assertThat(sut.compareTo(three)).isLessThan(0);
    }

    @Test
    void compareToEuqlOK() {
        // given

        // when

        // then
        assertThat(sut.compareTo(one)).isEqualTo(0);
    }


    @Test
    void compareToGreaterOK() {
        // given

        // when

        // then
        assertThat(three.compareTo(sut)).isGreaterThan(0);
    }
}
