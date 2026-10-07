package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class AdventureRunSessionOverridesTest {

    private static final String ID = SystemMessageKey.SM9.id();
    private static final String BUILT_IN = SystemMessageKey.SM9.defaultText();

    private GameContext gameContext;
    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        vocabulary = new Vocabulary();
    }

    // A loop that records what SM9 reads as while the turn runs, instead of parsing anything.
    private AdventureRunSession sessionWhoseTurnReads(List<String> seen, Map<String, String> overrides) {
        GameLoop recordingLoop = new GameLoop(new Parser(vocabulary), gameContext) {
            @Override
            public CommandOutcome processCommand(String anInput) {
                seen.add(SystemMessageKey.SM9.defaultText());
                return CommandOutcome.CONTINUE;
            }
        };
        return new AdventureRunSession(recordingLoop, gameContext, overrides);
    }

    @Test
    void submit_bindsTheSessionsOverridesForTheTurn_andUnbindsAfterwards() {
        List<String> seen = new ArrayList<>();
        AdventureRunSession session = sessionWhoseTurnReads(seen, Map.of(ID, "Carrying:"));

        session.submit("anything");

        assertThat(seen).containsExactly("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void submit_unbindsEvenWhenTheTurnThrows() {
        GameLoop throwingLoop = new GameLoop(new Parser(vocabulary), gameContext) {
            @Override
            public CommandOutcome processCommand(String anInput) {
                throw new IllegalStateException("boom");
            }
        };
        AdventureRunSession session = new AdventureRunSession(throwingLoop, gameContext, Map.of(ID, "Carrying:"));

        assertThatThrownBy(() -> session.submit("anything")).isInstanceOf(IllegalStateException.class);

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void runBound_bindsForTheStepOnly() {
        AdventureRunSession session = sessionWhoseTurnReads(new ArrayList<>(), Map.of(ID, "Carrying:"));

        String inside = session.runBound(SystemMessageKey.SM9::defaultText);

        assertThat(inside).isEqualTo("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void twoSessionsWithDifferentOverrides_eachSeeTheirOwn() {
        List<String> seenByA = new ArrayList<>();
        List<String> seenByB = new ArrayList<>();
        AdventureRunSession a = sessionWhoseTurnReads(seenByA, Map.of(ID, "Tragen:"));
        AdventureRunSession b = sessionWhoseTurnReads(seenByB, Map.of(ID, "Portant:"));

        a.submit("x");
        b.submit("x");
        a.submit("x");

        assertThat(seenByA).containsExactly("Tragen:", "Tragen:");
        assertThat(seenByB).containsExactly("Portant:");
    }

    @Test
    void supersede_makesTheNextSubmitReportTheGameEndedElsewhere() {
        List<String> seen = new ArrayList<>();
        AdventureRunSession session = sessionWhoseTurnReads(seen, Map.of());

        session.supersede();
        RunResult result = session.submit("look");

        assertThat(session.isGameOver()).isTrue();
        assertThat(result.gameOver()).isTrue();
        assertThat(result.lines()).containsExactly(AdventureRunSession.ENDED_ELSEWHERE_TEXT);
        assertThat(seen).isEmpty();
    }
}
