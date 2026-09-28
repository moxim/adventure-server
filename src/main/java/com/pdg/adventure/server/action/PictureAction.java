package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureAction extends AbstractAction {

    private final String pictureId;
    private final transient GameContext gameContext;

    public PictureAction(String aPictureId, GameContext aGameContext) {
        pictureId = aPictureId;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentPictureId(pictureId);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }
}
