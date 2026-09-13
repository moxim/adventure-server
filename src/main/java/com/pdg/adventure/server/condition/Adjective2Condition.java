package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Gates a command on the adjective describing the second noun the player actually typed (e.g.
 * "use spanner on <b>ancient</b> machine"), read from {@link GameContext#getCurrentAdjective2()}
 * - set fresh per sub-command by GameLoop, not carried on the command's own identity, so two
 * Response/Process rows sharing the same verb+adjective+noun can each be gated on a different
 * second adjective and both be tried by their shared {@link com.pdg.adventure.api.CommandChain}.
 * <p>
 * // TODO: Review needed - see the same note on Noun2Condition: "check for their presence" was
 * // read as "check for a specific, author-chosen adjective2", not a bare existence check.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Adjective2Condition extends AbstractCondition {

    @Getter
    private final String adjective2;
    private final transient GameContext gameContext;

    public Adjective2Condition(String anAdjective2, GameContext aGameContext) {
        adjective2 = anAdjective2;
        gameContext = aGameContext;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (adjective2.equals(gameContext.getCurrentAdjective2())) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        // No failure message - see PrepositionCondition for why.
        return result;
    }
}
