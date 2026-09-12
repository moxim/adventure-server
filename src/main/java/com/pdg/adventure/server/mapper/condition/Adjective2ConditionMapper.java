package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.Adjective2ConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.Adjective2Condition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "Adjective2Condition mapping")
public class Adjective2ConditionMapper extends PreConditionMapper<Adjective2ConditionData, Adjective2Condition> {

    private final GameContext gameContext;

    public Adjective2ConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public Adjective2Condition mapToBO(Adjective2ConditionData aConditionData) {
        Adjective2Condition result = new Adjective2Condition(aConditionData.getAdjective2Text(), gameContext);
        result.setId(aConditionData.getId());
        return result;
    }

    @Override
    public Adjective2ConditionData mapToDO(Adjective2Condition aCondition) {
        Adjective2ConditionData result = new Adjective2ConditionData();
        result.setAdjective2Text(aCondition.getAdjective2());
        result.setId(aCondition.getId());
        return result;
    }
}
