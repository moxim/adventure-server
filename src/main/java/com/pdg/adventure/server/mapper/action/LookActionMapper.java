package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.LookActionData;
import com.pdg.adventure.server.action.LookAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Look action mapper")
public class LookActionMapper extends ActionMapper<LookActionData, LookAction> {

    private final GameContext gameContext;

    public LookActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public LookAction mapToBO(LookActionData data) {
        return new LookAction(gameContext);
    }

    @Override
    public LookActionData mapToDO(LookAction action) {
        return new LookActionData();
    }
}
