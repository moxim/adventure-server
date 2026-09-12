package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Gates a command on the adverb word the player actually typed (e.g. "<b>slowly</b> open
 * chest"), read from {@link GameContext#getCurrentAdverb()} - set fresh per sub-command by
 * GameLoop, not carried on the command's own identity, so two Response/Process rows sharing the
 * same verb+adjective+noun can each be gated on a different adverb and both be tried by their
 * shared {@link com.pdg.adventure.api.CommandChain}.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AdverbCondition extends AbstractCondition {

    @Getter
    private final String adverb;
    private final transient GameContext gameContext;

    public AdverbCondition(String anAdverb, GameContext aGameContext) {
        adverb = anAdverb;
        gameContext = aGameContext;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (adverb.equals(gameContext.getCurrentAdverb())) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        // No failure message - see PrepositionCondition for why.
        return result;
    }
}
