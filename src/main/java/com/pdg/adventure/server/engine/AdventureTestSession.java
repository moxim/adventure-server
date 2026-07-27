package com.pdg.adventure.server.engine;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.server.exception.ReloadAdventureException;

/**
 * A single browser Test session, driving the shared GameLoop/GameContext singleton one command
 * at a time. Only created by {@link AdventureTestSessionFactory#start}, which has already primed
 * it with the opening room description.
 */
public class AdventureTestSession {

    private static final String PROMPT = "What now? > ";

    private final GameLoop gameLoop;
    private final GameContext gameContext;
    private boolean gameOver;

    AdventureTestSession(GameLoop aGameLoop, GameContext aGameContext) {
        gameLoop = aGameLoop;
        gameContext = aGameContext;
    }

    public TestResult submit(String rawInput) {
        if (gameOver) {
            return new TestResult(List.of(), true);
        }
        List<String> lines = new ArrayList<>();
        gameContext.setOutputSink(line -> {
            if (line != null && !line.isBlank() && !PROMPT.equals(line)) {
                lines.add(line);
            }
        });
        try {
            gameContext.preProcessCommands();
            GameLoop.CommandOutcome outcome = gameLoop.processCommand(rawInput);
            gameOver = outcome != GameLoop.CommandOutcome.CONTINUE;
        } catch (ReloadAdventureException unexpected) {
            // A Test session never registers the cross-adventure "load X" command, so this
            // should be unreachable — guarded so a surprise author workflow command can't leak
            // an uncaught exception into the Vaadin listener.
            lines.add("Something interrupted the game unexpectedly. Ending this test session.");
            gameOver = true;
        } finally {
            gameContext.setOutputSink(null);
        }
        return new TestResult(lines, gameOver);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public record TestResult(List<String> lines, boolean gameOver) {
    }
}
