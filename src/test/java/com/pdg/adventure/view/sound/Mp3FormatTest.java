package com.pdg.adventure.view.sound;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Mp3FormatTest {

    @Test
    void maxBytes_isSixMebibytes() {
        assertThat(Mp3Format.MAX_BYTES).isEqualTo(6 * 1024 * 1024);
    }

    @Test
    void id3Header_isAccepted() {
        assertThat(Mp3Format.looksLikeMp3(new byte[] {'I', 'D', '3', 4, 0, 0})).isTrue();
    }

    @Test
    void mpegLayer3FrameSync_isAccepted() {
        assertThat(Mp3Format.looksLikeMp3(new byte[] {(byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x00})).isTrue();
    }

    @Test
    void aacAdtsHeader_isRejected() {
        assertThat(Mp3Format.looksLikeMp3(new byte[] {(byte) 0xFF, (byte) 0xF1, (byte) 0x50, (byte) 0x80})).isFalse();
    }

    @Test
    void mpegLayer2FrameSync_isRejected() {
        assertThat(Mp3Format.looksLikeMp3(new byte[] {(byte) 0xFF, (byte) 0xFD, (byte) 0x90, 0x00})).isFalse();
    }

    @Test
    void pngHeader_isRejected() {
        assertThat(Mp3Format.looksLikeMp3(new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}))
                .isFalse();
    }

    @Test
    void nullEmptyAndTwoByteInputs_areRejected() {
        assertThat(Mp3Format.looksLikeMp3(null)).isFalse();
        assertThat(Mp3Format.looksLikeMp3(new byte[0])).isFalse();
        assertThat(Mp3Format.looksLikeMp3(new byte[] {'I', 'D'})).isFalse();
    }
}
