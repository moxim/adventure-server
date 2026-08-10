package com.pdg.adventure.server.support;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * The set of %s/%n$s argument positions a message template references. Used to validate that an
 * edited System Message still supplies every argument the engine's String.format call will pass -
 * dropping or misindexing one would only surface as a MissingFormatArgumentException at runtime,
 * not at save time.
 */
public record PlaceholderSpec(SortedSet<Integer> argumentPositions) {

    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+)\\$s|%s");

    public static PlaceholderSpec of(String aText) {
        String scanned = aText == null ? "" : aText.replace("%%", "");
        Matcher matcher = PLACEHOLDER.matcher(scanned);
        SortedSet<Integer> positions = new TreeSet<>();
        int plainCount = 0;
        boolean sawPlain = false;
        boolean sawPositional = false;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                sawPositional = true;
                positions.add(Integer.parseInt(matcher.group(1)));
            } else {
                sawPlain = true;
                plainCount++;
            }
        }
        if (sawPlain && sawPositional) {
            throw new IllegalArgumentException("Cannot mix %s and %n$s placeholders in one message: " + aText);
        }
        if (sawPlain) {
            IntStream.rangeClosed(1, plainCount).forEach(positions::add);
        }
        return new PlaceholderSpec(positions);
    }

    /** True if this spec references exactly the same argument positions as anOriginal - reordering is fine, dropping or adding one isn't. */
    public boolean satisfies(PlaceholderSpec anOriginal) {
        return argumentPositions.equals(anOriginal.argumentPositions());
    }

    /**
     * Convenience check used by both the service guard and the edit UI: true if aCandidateText
     * could safely replace anOriginalText's placeholders (same argument positions, reordering
     * allowed), false for a count/index mismatch or an invalid style mix - never throws.
     */
    public static boolean isValidReplacement(String anOriginalText, String aCandidateText) {
        try {
            return of(aCandidateText).satisfies(of(anOriginalText));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
