package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.PrepositionConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.PrepositionCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "PrepositionCondition mapping")
public class PrepositionConditionMapper extends PreConditionMapper<PrepositionConditionData, PrepositionCondition> {

    private final GameContext gameContext;

    public PrepositionConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PrepositionCondition mapToBO(PrepositionConditionData aConditionData) {
        PrepositionCondition result = new PrepositionCondition(aConditionData.getPrepositionText(), gameContext);
        result.setId(aConditionData.getId());
        return result;
    }

    @Override
    public PrepositionConditionData mapToDO(PrepositionCondition aCondition) {
        PrepositionConditionData result = new PrepositionConditionData();
        result.setPrepositionText(aCondition.getPreposition());
        result.setId(aCondition.getId());
        return result;
    }
}
