package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Gates a command on the cell of the picture grid the player clicked, read from
 * {@link GameContext#isClickAt(Integer, Integer)} - the click is recorded there only while GameLoop runs the
 * Processes for it, for the same reason {@link PrepositionCondition} reads what the player typed from the context:
 * the command itself carries no input. A typed turn never has a click, so there this is never true. A missing or
 * out-of-range coordinate (half-filled or imported data) simply never matches.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ClickedCondition extends AbstractCondition {

    @Getter
    private final Integer x;
    @Getter
    private final Integer y;
    private final transient GameContext gameContext;

    public ClickedCondition(Integer aX, Integer aY, GameContext aGameContext) {
        x = aX;
        y = aY;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (gameContext.isClickAt(x, y)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        // No failure message: a click elsewhere just means another Process (or none) is meant.
        return result;
    }

    /** The command this condition belongs to ran: the click was understood (see GameLoop#processClick). */
    @Override
    public void commandApplied() {
        if (gameContext.isClickAt(x, y)) {
            gameContext.markClickHandled();
        }
    }
}
