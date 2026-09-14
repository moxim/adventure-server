package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.LessThanConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.LessThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "LessThanCondition mapper")
public class LessThanConditionMapper extends PreConditionMapper<LessThanConditionData, LessThanCondition> {

    public LessThanConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public LessThanCondition mapToBO(LessThanConditionData data) {
        LessThanCondition result = new LessThanCondition(data.getVariableName(), data.getValue(),
                                                         getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public LessThanConditionData mapToDO(LessThanCondition condition) {
        LessThanConditionData result = new LessThanConditionData();
        result.setVariableName(condition.getVariableName());
        result.setValue(condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
