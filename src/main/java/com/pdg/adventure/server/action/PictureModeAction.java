package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Sets whether a location's picture is shown when the player arrives there (see {@link PictureMode}). Tells nothing;
 * the mode stays until another PictureModeAction changes it.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureModeAction extends AbstractAction {

    private final PictureMode mode;
    private final transient GameContext gameContext;

    public PictureModeAction(PictureMode aMode, GameContext aGameContext) {
        mode = aMode;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.setPictureMode(mode);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }
}
