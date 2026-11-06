package com.pdg.adventure.view.sound;

/**
 * A single byte range of an HTTP {@code Range} request, both ends inclusive. Browsers, Safari above all, ask for audio
 * in ranges and may refuse to play a server that answers every request with the whole file.
 */
record ByteRange(long start, long end) {

    /** The request asked for bytes the file does not have: answer 416. */
    static final ByteRange UNSATISFIABLE = new ByteRange(-1, -1);

    private static final String UNIT = "bytes=";

    long length() {
        return end - start + 1;
    }

    /**
     * Reads a {@code Range} header against a file of the given length.
     *
     * @return the range to send; {@link #UNSATISFIABLE} when it lies beyond the file; {@code null} when the whole file
     * is to be sent: no header, another unit, several ranges or a malformed one (all of which a server may ignore)
     */
    static ByteRange parse(String aHeader, long aLength) {
        if (aHeader == null || !aHeader.startsWith(UNIT) || aHeader.indexOf(',') >= 0) {
            return null;
        }
        String spec = aHeader.substring(UNIT.length()).trim();
        int dash = spec.indexOf('-');
        if (dash < 0) {
            return null;
        }
        try {
            String first = spec.substring(0, dash);
            String last = spec.substring(dash + 1);
            if (first.isEmpty()) {
                return suffix(last, aLength);
            }
            long start = Long.parseLong(first);
            if (last.isEmpty()) {
                return start >= aLength ? UNSATISFIABLE : new ByteRange(start, aLength - 1);
            }
            long end = Long.parseLong(last);
            if (end < start) {
                return null;
            }
            return start >= aLength ? UNSATISFIABLE : new ByteRange(start, Math.min(end, aLength - 1));
        } catch (NumberFormatException _) {
            return null;
        }
    }

    private static ByteRange suffix(String aCount, long aLength) {
        if (aCount.isEmpty()) {
            return null;
        }
        long count = Long.parseLong(aCount);
        if (count == 0 || aLength == 0) {
            return UNSATISFIABLE;
        }
        return new ByteRange(Math.max(0, aLength - count), aLength - 1);
    }
}
