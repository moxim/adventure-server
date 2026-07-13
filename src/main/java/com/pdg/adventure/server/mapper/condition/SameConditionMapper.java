package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.SameCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "SameCondition mapper")
public class SameConditionMapper extends PreConditionMapper<SameConditionData, SameCondition> {

    public SameConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public SameCondition mapToBO(SameConditionData data) {
        SameCondition result = new SameCondition(data.getVariableNameOne(), data.getVariableNameTwo(),
                                                  getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public SameConditionData mapToDO(SameCondition condition) {
        SameConditionData result = new SameConditionData();
        result.setVariableNameOne(condition.getVariableNameOne());
        result.setVariableNameTwo(condition.getVariableNameTwo());
        result.setId(condition.getId());
        return result;
    }
}
