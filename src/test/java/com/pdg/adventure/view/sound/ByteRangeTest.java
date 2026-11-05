package com.pdg.adventure.view.sound;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ByteRangeTest {

    @Test
    void noHeaderOrAnotherUnit_meansTheWholeFile() {
        assertThat(ByteRange.parse(null, 100)).isNull();
        assertThat(ByteRange.parse("", 100)).isNull();
        assertThat(ByteRange.parse("items=0-5", 100)).isNull();
    }

    @Test
    void fromStartToEnd() {
        assertThat(ByteRange.parse("bytes=10-19", 100)).isEqualTo(new ByteRange(10, 19));
    }

    @Test
    void openEndedRange_runsToTheLastByte() {
        assertThat(ByteRange.parse("bytes=90-", 100)).isEqualTo(new ByteRange(90, 99));
    }

    @Test
    void suffixRange_isTheLastBytes() {
        assertThat(ByteRange.parse("bytes=-10", 100)).isEqualTo(new ByteRange(90, 99));
        assertThat(ByteRange.parse("bytes=-500", 100)).isEqualTo(new ByteRange(0, 99));
    }

    @Test
    void anEndBeyondTheFile_isClippedToTheLastByte() {
        assertThat(ByteRange.parse("bytes=50-5000", 100)).isEqualTo(new ByteRange(50, 99));
    }

    @Test
    void aStartBeyondTheFile_isUnsatisfiable() {
        assertThat(ByteRange.parse("bytes=100-", 100)).isSameAs(ByteRange.UNSATISFIABLE);
        assertThat(ByteRange.parse("bytes=-0", 100)).isSameAs(ByteRange.UNSATISFIABLE);
        assertThat(ByteRange.parse("bytes=0-", 0)).isSameAs(ByteRange.UNSATISFIABLE);
    }

    @Test
    void malformedOrMultipleRanges_areIgnored_soTheWholeFileIsServed() {
        assertThat(ByteRange.parse("bytes=abc", 100)).isNull();
        assertThat(ByteRange.parse("bytes=20-10", 100)).isNull();
        assertThat(ByteRange.parse("bytes=0-1,5-6", 100)).isNull();
        assertThat(ByteRange.parse("bytes=-", 100)).isNull();
    }

    @Test
    void length_countsBothEnds() {
        assertThat(new ByteRange(10, 19).length()).isEqualTo(10);
    }
}
