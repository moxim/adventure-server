package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.AdverbConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.AdverbCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "AdverbCondition mapping")
public class AdverbConditionMapper extends PreConditionMapper<AdverbConditionData, AdverbCondition> {

    private final GameContext gameContext;

    public AdverbConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public AdverbCondition mapToBO(AdverbConditionData aConditionData) {
        AdverbCondition result = new AdverbCondition(aConditionData.getAdverbText(), gameContext);
        result.setId(aConditionData.getId());
        return result;
    }

    @Override
    public AdverbConditionData mapToDO(AdverbCondition aCondition) {
        AdverbConditionData result = new AdverbConditionData();
        result.setAdverbText(aCondition.getAdverb());
        result.setId(aCondition.getId());
        return result;
    }
}
