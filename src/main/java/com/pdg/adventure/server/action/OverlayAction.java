package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Lays a picture over one cell of the 10 x 10 grid of the picture on show, e.g. an open door over the closed one the
 * player just clicked. Does nothing, and still succeeds, when no picture is on show or the cell or picture is missing
 * (half-filled or imported data); tells nothing. See {@link GameContext#showOverlay(Integer, Integer, String)} for how
 * long the overlay stays.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class OverlayAction extends AbstractAction {

    private final Integer x;
    private final Integer y;
    private final String pictureId;
    private final transient GameContext gameContext;

    public OverlayAction(Integer aX, Integer aY, String aPictureId, GameContext aGameContext) {
        x = aX;
        y = aY;
        pictureId = aPictureId;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.showOverlay(x, y, pictureId);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }
}
