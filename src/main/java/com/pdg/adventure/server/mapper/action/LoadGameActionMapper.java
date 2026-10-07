package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.LoadGameActionData;
import com.pdg.adventure.server.action.LoadGameAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.GameStateSnapshotter;
import com.pdg.adventure.server.storage.service.SavedGameService;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "LoadGame action mapper")
public class LoadGameActionMapper extends ActionMapper<LoadGameActionData, LoadGameAction> {

    private final GameContext gameContext;
    private final GameStateSnapshotter snapshotter;
    private final SavedGameService savedGameService;

    public LoadGameActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext,
                                GameStateSnapshotter aSnapshotter, SavedGameService aSavedGameService) {
        super(aMapperSupporter);
        gameContext = aGameContext;
        snapshotter = aSnapshotter;
        savedGameService = aSavedGameService;
    }

    @Override
    public LoadGameAction mapToBO(LoadGameActionData data) {
        return new LoadGameAction(gameContext, snapshotter, savedGameService);
    }

    @Override
    public LoadGameActionData mapToDO(LoadGameAction action) {
        return new LoadGameActionData();
    }
}
