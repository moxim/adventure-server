package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.EqualsCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "EqualsCondition mapper")
public class EqualsConditionMapper extends PreConditionMapper<EqualsConditionData, EqualsCondition> {

    public EqualsConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public EqualsCondition mapToBO(EqualsConditionData data) {
        EqualsCondition result = new EqualsCondition(data.getVariableName(), data.getValue(),
                                                     getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public EqualsConditionData mapToDO(EqualsCondition condition) {
        EqualsConditionData result = new EqualsConditionData(condition.getVariableName(), condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
