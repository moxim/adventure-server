package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 10, description = "NotCondition mapping with dynamic action resolution")
public class NotConditionMapper extends PreConditionMapper<NotConditionData, NotCondition> {

    public NotConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(NotConditionData.class, NotCondition.class, this);
    }

    @Override
    public NotCondition mapToBO(NotConditionData aNotConditionData) {
        final PreCondition mapped = (PreCondition) getMapperSupporter().getMapper(
                aNotConditionData.getPreCondition().getClass()).mapToBO(aNotConditionData.getPreCondition());
        NotCondition result = new NotCondition(mapped);
        result.setId(aNotConditionData.getId());
        return result;
    }

    @Override
    public NotConditionData mapToDO(NotCondition aNotCondition) {
        final PreConditionData mapped = (PreConditionData) getMapperSupporter().getMapper(
                aNotCondition.getWrappedCondition().getClass()).mapToDO(aNotCondition.getWrappedCondition());
        NotConditionData result = new NotConditionData();
        result.setPreCondition(mapped);
        result.setId(aNotCondition.getId());
        return result;
    }
}
