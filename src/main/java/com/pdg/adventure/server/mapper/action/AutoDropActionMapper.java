package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.AutoDropActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.AutoDropAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "AutoDrop action mapper")
public class AutoDropActionMapper extends ActionMapper<AutoDropActionData, AutoDropAction> {

    private final GameContext gameContext;
    private final AdventureConfig adventureConfig;

    public AutoDropActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext,
                                @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        gameContext = aGameContext;
        adventureConfig = anAdventureConfig;
    }

    @Override
    public AutoDropAction mapToBO(AutoDropActionData data) {
        return new AutoDropAction(gameContext, adventureConfig.allItems());
    }

    @Override
    public AutoDropActionData mapToDO(AutoDropAction action) {
        return new AutoDropActionData();
    }
}
