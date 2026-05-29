package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.AndConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.AndCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 10, description = "AndCondition mapper")
public class AndConditionMapper extends PreConditionMapper<AndConditionData, AndCondition> {

    public AndConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(AndConditionData.class, AndCondition.class, this);
    }

    @Override
    public AndCondition mapToBO(AndConditionData data) {
        final PreCondition first = (PreCondition) getMapperSupporter()
                .getMapper(data.getPreCondition().getClass()).mapToBO(data.getPreCondition());
        final PreCondition second = (PreCondition) getMapperSupporter()
                .getMapper(data.getAnotherPreCondition().getClass()).mapToBO(data.getAnotherPreCondition());
        AndCondition result = new AndCondition(first, second);
        result.setId(data.getId());
        return result;
    }

    @Override
    public AndConditionData mapToDO(AndCondition condition) {
        final PreConditionData first = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getPreCondition().getClass()).mapToDO(condition.getPreCondition());
        final PreConditionData second = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getAnotherPreCondition().getClass()).mapToDO(condition.getAnotherPreCondition());
        AndConditionData result = new AndConditionData();
        result.setPreCondition(first);
        result.setAnotherPreCondition(second);
        result.setId(condition.getId());
        return result;
    }
}
