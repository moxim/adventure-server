package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.SaveGameActionData;
import com.pdg.adventure.server.action.SaveGameAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.GameStateSnapshotter;
import com.pdg.adventure.server.storage.service.SavedGameService;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "SaveGame action mapper")
public class SaveGameActionMapper extends ActionMapper<SaveGameActionData, SaveGameAction> {

    private final GameContext gameContext;
    private final GameStateSnapshotter snapshotter;
    private final SavedGameService savedGameService;

    public SaveGameActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext,
                                GameStateSnapshotter aSnapshotter, SavedGameService aSavedGameService) {
        super(aMapperSupporter);
        gameContext = aGameContext;
        snapshotter = aSnapshotter;
        savedGameService = aSavedGameService;
    }

    @Override
    public SaveGameAction mapToBO(SaveGameActionData data) {
        return new SaveGameAction(gameContext, snapshotter, savedGameService);
    }

    @Override
    public SaveGameActionData mapToDO(SaveGameAction action) {
        return new SaveGameActionData();
    }
}
