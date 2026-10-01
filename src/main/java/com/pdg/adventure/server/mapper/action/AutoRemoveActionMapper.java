package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.AutoRemoveActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.AutoRemoveAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "AutoRemove action mapper")
public class AutoRemoveActionMapper extends ActionMapper<AutoRemoveActionData, AutoRemoveAction> {

    private final GameContext gameContext;
    private final AdventureConfig adventureConfig;

    public AutoRemoveActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext,
                                @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        gameContext = aGameContext;
        adventureConfig = anAdventureConfig;
    }

    @Override
    public AutoRemoveAction mapToBO(AutoRemoveActionData data) {
        return new AutoRemoveAction(gameContext, adventureConfig.allItems());
    }

    @Override
    public AutoRemoveActionData mapToDO(AutoRemoveAction action) {
        return new AutoRemoveActionData();
    }
}
