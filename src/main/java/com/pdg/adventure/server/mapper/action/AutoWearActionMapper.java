package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.AutoWearActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.AutoWearAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "AutoWear action mapper")
public class AutoWearActionMapper extends ActionMapper<AutoWearActionData, AutoWearAction> {

    private final GameContext gameContext;
    private final AdventureConfig adventureConfig;

    public AutoWearActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext,
                                @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        gameContext = aGameContext;
        adventureConfig = anAdventureConfig;
    }

    @Override
    public AutoWearAction mapToBO(AutoWearActionData data) {
        return new AutoWearAction(gameContext, adventureConfig.allItems());
    }

    @Override
    public AutoWearActionData mapToDO(AutoWearAction action) {
        return new AutoWearActionData();
    }
}
