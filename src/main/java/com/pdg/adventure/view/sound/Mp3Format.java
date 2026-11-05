package com.pdg.adventure.view.sound;

/** What counts as an mp3 sound: its size limit and a check of the first bytes of the file. */
public final class Mp3Format {

    /**
     * The most bytes of one sound file: 6 MiB (the picture editor counts "2MB" the same way).
     * TODO: Review needed — the product owner said "6MB"; read as 6 MiB following the picture editor.
     */
    public static final int MAX_BYTES = 6 * 1024 * 1024;

    private static final int LAYER_III = 0x01;

    private Mp3Format() {
    }

    /**
     * Whether the bytes start like an mp3: an ID3 tag, or an MPEG audio frame header (sync bits, Layer III). A file
     * merely named {@code .mp3}, a PNG, or an AAC stream (ADTS) is not.
     */
    public static boolean looksLikeMp3(byte[] someBytes) {
        if (someBytes == null || someBytes.length < 3) {
            return false;
        }
        if (someBytes[0] == 'I' && someBytes[1] == 'D' && someBytes[2] == '3') {
            return true;
        }
        int second = someBytes[1] & 0xFF;
        boolean frameSync = (someBytes[0] & 0xFF) == 0xFF && (second & 0xE0) == 0xE0;
        boolean validVersion = ((second >> 3) & 0x03) != 0x01; // 01 is reserved
        boolean layerIII = ((second >> 1) & 0x03) == LAYER_III;
        return frameSync && validVersion && layerIII;
    }
}
