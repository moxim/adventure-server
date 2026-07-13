package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.LowerThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "LowerThanCondition mapper")
public class LowerThanConditionMapper extends PreConditionMapper<LowerThanConditionData, LowerThanCondition> {

    public LowerThanConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public LowerThanCondition mapToBO(LowerThanConditionData data) {
        LowerThanCondition result = new LowerThanCondition(data.getVariableName(), data.getValue(),
                                                           getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public LowerThanConditionData mapToDO(LowerThanCondition condition) {
        LowerThanConditionData result = new LowerThanConditionData();
        result.setVariableName(condition.getVariableName());
        result.setValue(condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
