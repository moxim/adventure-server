package com.pdg.adventure.server.engine;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.server.exception.ReloadAdventureException;

/**
 * A single interactive play session, driving the shared GameLoop/GameContext singleton one
 * command at a time. Used both when an author clicks "Test" on their own adventure and when a
 * player clicks "Run Adventure" on one they're assigned to. Only created by
 * {@link AdventureRunSessionFactory#start}, which has already primed it with the opening room
 * description.
 */
public class AdventureRunSession {

    private static final String PROMPT = "What now? > ";

    private final GameLoop gameLoop;
    private final GameContext gameContext;
    private boolean gameOver;

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext) {
        gameLoop = aGameLoop;
        gameContext = aGameContext;
    }

    public RunResult submit(String rawInput) {
        if (gameOver) {
            return new RunResult(List.of(), true);
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
