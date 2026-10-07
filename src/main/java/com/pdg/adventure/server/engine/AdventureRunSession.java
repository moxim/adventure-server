package com.pdg.adventure.server.engine;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * A single interactive play session, driving the browser session's GameLoop/GameContext one command at a
 * time. Used both when an author clicks "Test" on their own adventure and when a player clicks "Run Adventure"
 * on one they're assigned to. Only created by {@link AdventureRunSessionFactory}; the caller
 * (AdventureRunView) still renders the opening room itself, through {@link #runBound}.
 * <p>
 * The session carries its adventure's system-message overrides and binds them around every turn (and around
 * anything else run through {@link #runBound}), so different adventures can be played at the same time.
 */
public class AdventureRunSession {

    static final String ENDED_ELSEWHERE_TEXT = "This game was ended in another tab.";

    private final GameLoop gameLoop;
    @Getter
    private final GameContext gameContext;
    private final Map<String, String> systemMessageOverrides;
    private boolean gameOver;
    private boolean supersededElsewhere;

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext) {
        this(aGameLoop, aGameContext, Map.of());
    }

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext, Map<String, String> anOverridesByKeyId) {
        gameLoop = aGameLoop;
        gameContext = aGameContext;
        systemMessageOverrides = Map.copyOf(anOverridesByKeyId);
    }

    public RunResult submit(String rawInput) {
        if (supersededElsewhere) {
            return new RunResult(List.of(ENDED_ELSEWHERE_TEXT), true);
        }
        if (gameOver) {
            return new RunResult(List.of(), true);
        }
        return runBound(() -> playTurn(rawInput));
    }

    /**
     * Runs the step with this session's system-message overrides bound to the current thread. For work done
     * outside {@link #submit} that still prints engine text, e.g. rendering the opening room.
     */
    public <T> T runBound(Supplier<T> aStep) {
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(systemMessageOverrides)) {
            return aStep.get();
        }
    }

    /** Ends this session because the player started the same game anew in another tab. */
    public void supersede() {
        supersededElsewhere = true;
        gameOver = true;
    }

    private RunResult playTurn(String rawInput) {
        List<String> lines = new ArrayList<>();
        gameContext.setOutputSink(line -> {
            if (line != null && !line.isBlank() && !SystemMessageKey.SM2.defaultText().equals(line)) {
                lines.add(line);
            }
        });
        try {
            GameLoop.CommandOutcome outcome = gameLoop.processCommand(rawInput);
            gameOver = outcome != GameLoop.CommandOutcome.CONTINUE;
        } catch (ReloadAdventureException unexpected) {
            // A run session never registers the cross-adventure "load X" command, so this
            // should be unreachable — guarded so a surprise author workflow command can't leak
            // an uncaught exception into the Vaadin listener.
            lines.add("Something interrupted the game unexpectedly. Ending this session.");
            gameOver = true;
        } finally {
            gameContext.setOutputSink(null);
        }
        return new RunResult(lines, gameOver);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public record RunResult(List<String> lines, boolean gameOver) {
    }
}
