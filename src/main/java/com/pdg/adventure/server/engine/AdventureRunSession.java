package com.pdg.adventure.server.engine;

import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

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

    /** What the game asks when it waits for the player: one of these, picked at random. */
    private static final List<SystemMessageKey> PROMPTS = List.of(SystemMessageKey.SM2, SystemMessageKey.SM3,
                                                                  SystemMessageKey.SM4, SystemMessageKey.SM5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GameLoop gameLoop;
    @Getter
    private final GameContext gameContext;
    private final Map<String, String> systemMessageOverrides;
    private final VariableProvider variableProvider;
    @Getter
    private boolean gameOver;
    private boolean supersededElsewhere;

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext) {
        this(aGameLoop, aGameContext, Map.of());
    }

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext, Map<String, String> anOverridesByKeyId) {
        this(aGameLoop, aGameContext, anOverridesByKeyId, new VariableProvider());
    }

    /** @param aVariableProvider the one the game's actions and conditions use; its PROMPT variable steers {@link #prompt()} */
    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext, Map<String, String> anOverridesByKeyId,
                        VariableProvider aVariableProvider) {
        gameLoop = aGameLoop;
        gameContext = aGameContext;
        systemMessageOverrides = Map.copyOf(anOverridesByKeyId);
        variableProvider = aVariableProvider;
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

    /**
     * The question that tells the player the game waits for input, e.g. "What now?": one of system messages 2 to 5,
     * in the adventure's own wording. Shown after the opening room and after every turn. The PROMPT variable steers
     * which one, see {@link #promptKeyFor(int)}; while it is not set (0), chance decides.
     */
    public String prompt() {
        int steering = variableProvider.get(VariableProvider.PROMPT_VARIABLE_NAME).map(Variable::value).orElse(0);
        SystemMessageKey key = promptKeyFor(steering).orElseGet(() -> PROMPTS.get(RANDOM.nextInt(PROMPTS.size())));
        return runBound(key::defaultText);
    }

    /**
     * The question the PROMPT variable asks for. 2 to 5 pick system message 2 to 5; a greater value wraps around
     * ({@code 2 + (value - 2) % 4}: 6 is SM2 again, 7 SM3, ... 10 SM2, ...), so any number an author arrives at works.
     * Anything below 2 leaves the choice to chance (empty result).
     * <p>
     */
    static Optional<SystemMessageKey> promptKeyFor(int aSteeringValue) {
        if (aSteeringValue < 2) {
            return Optional.empty();
        }
        return Optional.of(PROMPTS.get((aSteeringValue - 2) % PROMPTS.size()));
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
        return new RunResult(lines, gameOver, gameOver ? "" : prompt());
    }

    /**
     * @param lines    what the game printed for the turn
     * @param gameOver whether the game has ended
     * @param prompt   the question to show when the game waits for the next input; empty if there is none (the game
     *                 is over)
     */
    public record RunResult(List<String> lines, boolean gameOver, String prompt) {
        public RunResult(List<String> lines, boolean gameOver) {
            this(lines, gameOver, "");
        }
    }
}
