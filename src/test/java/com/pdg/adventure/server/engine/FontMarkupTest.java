package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.engine.FontMarkup.Segment;

class FontMarkupTest {

    private static final String NL = System.lineSeparator();

    @Test
    void aMessageWithoutAFontIsLeftAlone() {
        assertThat(FontMarkup.wrap("hello", AdventureFont.DEFAULT)).isEqualTo("hello");
        assertThat(FontMarkup.wrap("hello", null)).isEqualTo("hello");
    }

    @Test
    void aBlankMessageIsNeverWrappedSoItStaysBlank() {
        assertThat(FontMarkup.wrap("", AdventureFont.CINZEL)).isEmpty();
        assertThat(FontMarkup.wrap("  \n ", AdventureFont.CINZEL)).isEqualTo("  \n ");
        assertThat(FontMarkup.wrap(null, AdventureFont.CINZEL)).isNull();
    }

    @Test
    void wrappedTextRoundTripsThroughSplit() {
        assertThat(FontMarkup.split(FontMarkup.wrap("the note", AdventureFont.SPECIAL_ELITE)))
                .containsExactly(new Segment("the note", AdventureFont.SPECIAL_ELITE));
    }

    @Test
    void textWithoutMarkersIsOneUntouchedDefaultSegment() {
        String text = "You see a key." + NL + "It is rusty." + NL;

        assertThat(FontMarkup.split(text)).containsExactly(new Segment(text, AdventureFont.DEFAULT));
    }

    @Test
    void blankOrMissingTextHasNoSegments() {
        assertThat(FontMarkup.split(null)).isEmpty();
        assertThat(FontMarkup.split("")).isEmpty();
        assertThat(FontMarkup.split("  " + NL)).isEmpty();
    }

    @Test
    void aMarkedMessageBetweenPlainLinesBecomesThreeSegmentsWithoutTheLineBreaks() {
        String text = "You enter the study." + NL + FontMarkup.wrap("Meet me at midnight.", AdventureFont.SPECIAL_ELITE)
                      + NL + "The clock ticks.";

        List<Segment> segments = FontMarkup.split(text);

        assertThat(segments).containsExactly(
                new Segment("You enter the study.", AdventureFont.DEFAULT),
                new Segment("Meet me at midnight.", AdventureFont.SPECIAL_ELITE),
                new Segment("The clock ticks.", AdventureFont.DEFAULT));
    }

    @Test
    void twoMarkedMessagesInARowStaySeparateEvenWithTheSameFont() {
        String text = FontMarkup.wrap("first", AdventureFont.CINZEL) + NL + FontMarkup.wrap("second", AdventureFont.CINZEL);

        assertThat(FontMarkup.split(text)).containsExactly(new Segment("first", AdventureFont.CINZEL),
                                                           new Segment("second", AdventureFont.CINZEL));
    }

    @Test
    void markersSurviveTrimAndStripBecauseWorkflowTrimsJoinedResultText() {
        // Workflow.getExecutionResult(...) calls String.trim() on joined text, which removes every
        // character <= U+0020 at the edges - so the markers must not be control characters.
        String wrapped = FontMarkup.wrap("note", AdventureFont.CINZEL);

        assertThat(wrapped.trim()).isEqualTo(wrapped);
        assertThat(wrapped.strip()).isEqualTo(wrapped);
    }

    @Test
    void stripRemovesTheMarkersForPlainOutput() {
        String text = "before" + NL + FontMarkup.wrap("note", AdventureFont.CINZEL) + NL + "after";

        assertThat(FontMarkup.strip(text)).isEqualTo("before" + NL + "note" + NL + "after");
        assertThat(FontMarkup.strip("plain")).isEqualTo("plain");
        assertThat(FontMarkup.strip(null)).isNull();
    }

    @Test
    void markerCharactersInAuthorTextCannotForgeOrBreakMarkup() {
        String hostile = "aINTERbc";

        assertThat(FontMarkup.split(FontMarkup.wrap(hostile, AdventureFont.CINZEL)))
                .containsExactly(new Segment("aINTERbc", AdventureFont.CINZEL));
    }

    @Test
    void anUnknownFontNameFallsBackToTheDefaultFontButKeepsTheText() {
        String text = "NO_SUCH_FONTstill shown";

        assertThat(FontMarkup.split(text)).containsExactly(new Segment("still shown", AdventureFont.DEFAULT));
    }

    @Test
    void malformedMarkupNeverThrowsAndNeverLeaksMarkers() {
        for (String broken : List.of("CINZEL", "CINZELunterminated", "strayend", "")) {
            List<Segment> segments = FontMarkup.split(broken);

            assertThat(segments).extracting(Segment::text).allSatisfy(text ->
                    assertThat(text).doesNotContain("", "", ""));
        }
    }
}
