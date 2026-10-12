package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.TurnsActionData;
import com.pdg.adventure.server.action.TurnsAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Turns action mapper")
public class TurnsActionMapper extends ActionMapper<TurnsActionData, TurnsAction> {

    private final GameContext gameContext;

    public TurnsActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public TurnsAction mapToBO(TurnsActionData actionData) {
        return new TurnsAction(gameContext::tell, getMapperSupporter().getVariableProvider());
    }

    @Override
    public TurnsActionData mapToDO(TurnsAction action) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
