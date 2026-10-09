package com.pdg.adventure.server.engine;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureFont;

/**
 * Carries a message's font from the engine to the run view inside the result text.
 * <p>
 * Actions don't print: each returns a result message, the messages of a command/chain are joined into one
 * string and printed once per turn. A message that has a font therefore wraps itself as
 * {@code START <font name> SEP <text> END}; {@code AdventureRunView} splits the turn's text back into one
 * message per font, and console output has the markers {@link #strip stripped}.
 * <p>
 * The markers are Unicode private-use characters on purpose: {@code Workflow} calls {@code String.trim()} on
 * joined result text, which would silently remove control characters (everything &lt;= U+0020) at the edges.
 * <p>
 */
public final class FontMarkup {
    private static final char START = '';
    private static final char SEP = '';
    private static final char END = '';
    private static final String MARKER_CHARS = "[" + START + SEP + END + "]";
    private static final String FONT_HEADER = START + "[^" + SEP + END + "]*" + SEP;

    private FontMarkup() {
    }

    /** One run of text and the font it is shown in; {@link AdventureFont#DEFAULT} means "no override". */
    public record Segment(String text, AdventureFont font) {
    }

    /**
     * Wraps the text for the given font. {@link AdventureFont#DEFAULT} (or null) and blank text are returned as is,
     * so adventures without message fonts produce exactly the output they always did.
     */
    public static String wrap(String aText, AdventureFont aFont) {
        if (aFont == null || aFont == AdventureFont.DEFAULT || aText == null || aText.isBlank()) {
            return aText;
        }
        // an author's text must not be able to forge or break the markup
        return START + aFont.name() + SEP + aText.replaceAll(MARKER_CHARS, "") + END;
    }

    /**
     * Splits a turn's text into its font runs. Text without any marker is one untouched {@code DEFAULT}
     * segment. When markers are present, the line breaks around each run are dropped (they only separated
     * messages) and blank runs vanish. Malformed markup never throws and never leaves a marker in the text.
     */
    public static List<Segment> split(String aText) {
        List<Segment> segments = new ArrayList<>();
        if (aText == null || aText.isBlank()) {
            return segments;
        }
        if (!containsMarker(aText)) {
            segments.add(new Segment(aText, AdventureFont.DEFAULT));
            return segments;
        }

        StringBuilder plain = new StringBuilder();
        int i = 0;
        while (i < aText.length()) {
            char c = aText.charAt(i);
            if (c == START) {
                int sep = aText.indexOf(SEP, i + 1);
                if (sep < 0) {
                    plain.append(aText.substring(i + 1));   // no font header: show what follows as plain text
                    break;
                }
                int end = aText.indexOf(END, sep + 1);
                String body = findNextBodyOfText(aText, end, sep);
                addSegment(segments, plain.toString(), AdventureFont.DEFAULT);
                plain.setLength(0);
                addSegment(segments, body, fontNamed(aText.substring(i + 1, sep)));
                i = getNextValueForI(aText, end);
            } else {
                appendCifNotSEPorEND(c, plain);
                i++;
            }
        }
        addSegment(segments, plain.toString(), AdventureFont.DEFAULT);
        return segments;
    }

    private static @NonNull String findNextBodyOfText(final String aText, final int end, final int sep) {
        return end < 0 ? aText.substring(sep + 1) : aText.substring(sep + 1, end);
    }

    private static int getNextValueForI(final String aText, final int end) {
        return end < 0 ? aText.length() : end + 1;
    }

    private static void appendCifNotSEPorEND(final char c, final StringBuilder plain) {
        if (c != SEP && c != END) {
            plain.append(c);
        }
    }

    /** The text without any markup, for output that has no fonts (the console). */
    public static String strip(String aText) {
        if (aText == null || !containsMarker(aText)) {
            return aText;
        }
        return aText.replaceAll(FONT_HEADER, "").replaceAll(MARKER_CHARS, "");
    }

    private static boolean containsMarker(String aText) {
        return aText.indexOf(START) >= 0 || aText.indexOf(SEP) >= 0 || aText.indexOf(END) >= 0;
    }

    private static void addSegment(List<Segment> someSegments, String aText, AdventureFont aFont) {
        String text = trimLineBreaks(aText.replaceAll(MARKER_CHARS, ""));
        if (!text.isBlank()) {
            someSegments.add(new Segment(text, aFont));
        }
    }

    private static String trimLineBreaks(String aText) {
        int from = 0;
        int to = aText.length();
        while (from < to && (aText.charAt(from) == '\n' || aText.charAt(from) == '\r')) {
            from++;
        }
        while (to > from && (aText.charAt(to - 1) == '\n' || aText.charAt(to - 1) == '\r')) {
            to--;
        }
        return aText.substring(from, to);
    }

    private static AdventureFont fontNamed(String aName) {
        try {
            return AdventureFont.valueOf(aName);
        } catch (IllegalArgumentException _) {
            return AdventureFont.DEFAULT;   // e.g. a font retired after the message was written
        }
    }
}
