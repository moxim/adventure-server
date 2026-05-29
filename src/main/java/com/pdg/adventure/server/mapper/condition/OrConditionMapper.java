package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.OrConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.OrCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 10, description = "OrCondition mapper")
public class OrConditionMapper extends PreConditionMapper<OrConditionData, OrCondition> {

    public OrConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(OrConditionData.class, OrCondition.class, this);
    }

    @Override
    public OrCondition mapToBO(OrConditionData data) {
        final PreCondition first = (PreCondition) getMapperSupporter()
                .getMapper(data.getPreCondition().getClass()).mapToBO(data.getPreCondition());
        final PreCondition second = (PreCondition) getMapperSupporter()
                .getMapper(data.getAnotherPreCondition().getClass()).mapToBO(data.getAnotherPreCondition());
        OrCondition result = new OrCondition(first, second);
        result.setId(data.getId());
        return result;
    }

    @Override
    public OrConditionData mapToDO(OrCondition condition) {
        final PreConditionData first = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getPreCondition().getClass()).mapToDO(condition.getPreCondition());
        final PreConditionData second = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getAnotherPreCondition().getClass()).mapToDO(condition.getAnotherPreCondition());
        OrConditionData result = new OrConditionData();
        result.setPreCondition(first);
        result.setAnotherPreCondition(second);
        result.setId(condition.getId());
        return result;
    }
}
