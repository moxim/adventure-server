package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Gates a command on the second noun the player actually typed (e.g. "use spanner on ancient
 * <b>machine</b>"), read from {@link GameContext#getCurrentNoun2()} - set fresh per sub-command
 * by GameLoop, not carried on the command's own identity, so two Response/Process rows sharing
 * the same verb+adjective+noun can each be gated on a different second noun and both be tried by
 * their shared {@link com.pdg.adventure.api.CommandChain}.
 * <p>
 * // TODO: Review needed - "check for their presence" was read as "check for a specific,
 * // author-chosen noun2" (matching the just-built PrepositionCondition/AdverbCondition
 * // precedent, picked via a vocabulary dropdown) rather than a bare existence check ("was a
 * // second noun given at all, regardless of which"). If the latter is what's wanted, this
 * // becomes a simple !gameContext.getCurrentNoun2().isEmpty() check instead.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Noun2Condition extends AbstractCondition {

    @Getter
    private final String noun2;
    private final transient GameContext gameContext;

    public Noun2Condition(String aNoun2, GameContext aGameContext) {
        noun2 = aNoun2;
        gameContext = aGameContext;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (noun2.equals(gameContext.getCurrentNoun2())) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        // No failure message - see PrepositionCondition for why.
        return result;
    }
}
