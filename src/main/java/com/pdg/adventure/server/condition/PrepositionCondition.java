package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Gates a command on the preposition word the player actually typed (e.g. "switch lamp
 * <b>on</b>" vs "switch lamp <b>off</b>"), read from {@link GameContext#getCurrentPreposition()}
 * - set fresh per sub-command by GameLoop, not carried on the command's own identity, so two
 * Response/Process rows sharing the same verb+adjective+noun can each be gated on a different
 * preposition and both be tried by their shared {@link com.pdg.adventure.api.CommandChain}.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PrepositionCondition extends AbstractCondition {

    @Getter
    private final String preposition;
    private final transient GameContext gameContext;

    public PrepositionCondition(String aPreposition, GameContext aGameContext) {
        preposition = aPreposition;
        gameContext = aGameContext;
    }

    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (preposition.equals(gameContext.getCurrentPreposition())) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        // No failure message: a mismatch here just means a sibling row (or nothing) should
        // handle this input, not that the player did something wrong - see GenericCommandChain
        // and Workflow, which try every row sharing a spec and only surface the last failure's
        // message if none of them succeeded.
        return result;
    }
}
