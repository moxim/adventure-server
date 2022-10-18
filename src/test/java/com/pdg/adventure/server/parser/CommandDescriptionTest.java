package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CommandDescriptionTest {

    CommandDescription sut = new CommandDescription("1", "2");
    CommandDescription one = new CommandDescription("1", "2");
        CommandDescription three = new CommandDescription("3");

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

    @Test
    void compareToThrowsException() {
        // given

        // when

        Throwable thrown = catchThrowable(() -> {sut.compareTo(1);});

        // then
        assertThat(thrown).isInstanceOf(ClassCastException.class);
    }



}
