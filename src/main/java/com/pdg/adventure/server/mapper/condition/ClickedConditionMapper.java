package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.ClickedConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.ClickedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "ClickedCondition mapping")
public class ClickedConditionMapper extends PreConditionMapper<ClickedConditionData, ClickedCondition> {

    private final GameContext gameContext;

    public ClickedConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public ClickedCondition mapToBO(ClickedConditionData aConditionData) {
        ClickedCondition result = new ClickedCondition(aConditionData.getX(), aConditionData.getY(), gameContext);
        result.setId(aConditionData.getId());
        return result;
    }

    @Override
    public ClickedConditionData mapToDO(ClickedCondition aCondition) {
        ClickedConditionData result = new ClickedConditionData();
        result.setX(aCondition.getX());
        result.setY(aCondition.getY());
        result.setId(aCondition.getId());
        return result;
    }
}
