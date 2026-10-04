package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.AutoTakeActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.AutoTakeAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "AutoTake action mapper")
public class AutoTakeActionMapper extends ActionMapper<AutoTakeActionData, AutoTakeAction> {

    private final GameContext gameContext;
    private final AdventureConfig adventureConfig;

    public AutoTakeActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext,
                                @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        gameContext = aGameContext;
        adventureConfig = anAdventureConfig;
    }

    @Override
    public AutoTakeAction mapToBO(AutoTakeActionData data) {
        return new AutoTakeAction(gameContext, adventureConfig.allItems());
    }

    @Override
    public AutoTakeActionData mapToDO(AutoTakeAction action) {
        return new AutoTakeActionData();
    }
}
