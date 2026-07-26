package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.ChanceConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.ChanceCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "ChanceCondition mapper")
public class ChanceConditionMapper extends PreConditionMapper<ChanceConditionData, ChanceCondition> {

    public ChanceConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public ChanceCondition mapToBO(ChanceConditionData data) {
        ChanceCondition result = new ChanceCondition(data.getValue());
        result.setId(data.getId());
        return result;
    }

    @Override
    public ChanceConditionData mapToDO(ChanceCondition condition) {
        ChanceConditionData result = new ChanceConditionData();
        result.setValue(condition.getChance());
        result.setId(condition.getId());
        return result;
    }
}
