package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.RandomActionData;
import com.pdg.adventure.server.action.RandomAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Random action mapper")
public class RandomActionMapper extends ActionMapper<RandomActionData, RandomAction> {

    public RandomActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public RandomAction mapToBO(RandomActionData from) {
        final var action = new RandomAction(from.getName(), getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public RandomActionData mapToDO(RandomAction from) {
        RandomActionData actionData = new RandomActionData();
        actionData.setName(from.getName());
        actionData.setId(from.getId());
        return actionData;
    }
}
