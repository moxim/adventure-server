package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureFont;

class GameContextConsoleOutputTest {

    @Test
    void theConsoleShowsAMessageWithAFontWithoutItsMarkers() {
        GameContext gameContext = new GameContext();
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            gameContext.tell("before" + System.lineSeparator()
                             + FontMarkup.wrap("the note", AdventureFont.SPECIAL_ELITE));
        } finally {
            System.setOut(original);
        }

        assertThat(captured.toString(StandardCharsets.UTF_8)).contains("the note")
                .doesNotContain("", "", "", "SPECIAL_ELITE");
    }

    @Test
    void aBrowserSessionsSinkStillReceivesTheMarkersSoTheRunViewCanSplitThem() {
        GameContext gameContext = new GameContext();
        List<String> lines = new ArrayList<>();
        gameContext.setOutputSink(lines::add);
        try {
            gameContext.tell(FontMarkup.wrap("the note", AdventureFont.SPECIAL_ELITE));
        } finally {
            gameContext.setOutputSink(null);
        }

        assertThat(FontMarkup.split(lines.getFirst()))
                .containsExactly(new FontMarkup.Segment("the note", AdventureFont.SPECIAL_ELITE));
    }
}
