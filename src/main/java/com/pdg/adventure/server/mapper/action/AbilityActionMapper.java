package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.AbilityActionData;
import com.pdg.adventure.server.action.AbilityAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Ability action mapper")
public class AbilityActionMapper extends ActionMapper<AbilityActionData, AbilityAction> {

    private final GameContext gameContext;

    public AbilityActionMapper(GameContext aGameContext, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public AbilityAction mapToBO(AbilityActionData actionData) {
        return new AbilityAction(actionData.getMaxWeight(), actionData.getMaxItems(), gameContext);
    }

    @Override
    public AbilityActionData mapToDO(AbilityAction action) {
        AbilityActionData data = new AbilityActionData();
        data.setMaxWeight(action.getMaxWeight());
        data.setMaxItems(action.getMaxItems());
        return data;
    }
}
