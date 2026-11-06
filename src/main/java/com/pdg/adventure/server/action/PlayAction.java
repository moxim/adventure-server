package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Plays a sound of the adventure {@code times} times in a row, starting at once, or stops every copy of it with 0.
 * Tells nothing and always succeeds: an unknown sound, a missing count or a browser that cannot play is no error.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PlayAction extends AbstractAction {

    private final String soundId;
    private final Integer times;
    private final transient GameContext gameContext;

    public PlayAction(String aSoundId, Integer aTimes, GameContext aGameContext) {
        soundId = aSoundId;
        times = aTimes;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        if (times != null) {
            gameContext.playSound(soundId, times);
        }
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }
}
