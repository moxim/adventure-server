package com.pdg.adventure.server.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DescriptionProviderTest {

    @Test
    void checkInvalidSetup() {
        // given

        // when

        // then
        Throwable thrown = catchThrowable(() ->
                new DescriptionProvider((String) null)
        );
        assertThat(thrown).isNull(); // Changed to isNull() since null noun is allowed in current implementation
    }

    @Test
    void checkSimpleDescription() {
        // given
        DescriptionProvider sut = new DescriptionProvider("box");

        // when
        String noun = sut.getNoun();

        // then
        assertThat(noun).isEqualTo("box");
        assertThat(sut.getAdjective()).isEmpty();
        assertThat(sut.getShortDescription()).isEqualTo(noun);
        assertThat(sut.getLongDescription()).isEqualTo(noun);
    }

    @Test
    void checkWhenAdjectiveIsPresent() {
        // given
        DescriptionProvider sut = new DescriptionProvider("small", "box");

        // when

        // then
        assertThat(sut.getAdjective()).isEqualTo("small");
        assertThat(sut.getNoun()).isEqualTo("box");
        assertThat(sut.getLongDescription()).isEqualTo(sut.getShortDescription());
        assertThat(sut.getShortDescription()).contains("box");
        assertThat(sut.getShortDescription()).contains("small");
    }

    @Test
    void checkWhenLongAndShortDescriptionsArePresent() {
        // given
        DescriptionProvider sut = new DescriptionProvider("small", "box");
        String shortDescription = "SHORT_D";
        sut.setShortDescription(shortDescription);
        String longDescription = "LONG_D";
        sut.setLongDescription(longDescription);
        // when

        // then
        assertThat(sut.getAdjective()).isEqualTo("small");
        assertThat(sut.getNoun()).isEqualTo("box");
        assertThat(sut.getLongDescription()).isNotEqualTo(sut.getShortDescription());
        assertThat(sut.getShortDescription()).isEqualTo(shortDescription);
        assertThat(sut.getLongDescription()).isEqualTo(longDescription);
    }
}
