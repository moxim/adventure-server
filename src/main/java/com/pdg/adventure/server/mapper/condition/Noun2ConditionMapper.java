package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.Noun2ConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.Noun2Condition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "Noun2Condition mapping")
public class Noun2ConditionMapper extends PreConditionMapper<Noun2ConditionData, Noun2Condition> {

    private final GameContext gameContext;

    public Noun2ConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public Noun2Condition mapToBO(Noun2ConditionData aConditionData) {
        Noun2Condition result = new Noun2Condition(aConditionData.getNoun2Text(), gameContext);
        result.setId(aConditionData.getId());
        return result;
    }

    @Override
    public Noun2ConditionData mapToDO(Noun2Condition aCondition) {
        Noun2ConditionData result = new Noun2ConditionData();
        result.setNoun2Text(aCondition.getNoun2());
        result.setId(aCondition.getId());
        return result;
    }
}
