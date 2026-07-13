package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.GreaterThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "GreaterThanCondition mapper")
public class GreaterThanConditionMapper extends PreConditionMapper<GreaterThanConditionData, GreaterThanCondition> {

    public GreaterThanConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public GreaterThanCondition mapToBO(GreaterThanConditionData data) {
        GreaterThanCondition result = new GreaterThanCondition(data.getVariableName(), data.getValue(),
                                                               getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public GreaterThanConditionData mapToDO(GreaterThanCondition condition) {
        GreaterThanConditionData result = new GreaterThanConditionData();
        result.setVariableName(condition.getVariableName());
        result.setValue(condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
