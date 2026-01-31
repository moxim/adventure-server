package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "HereCondition mapping with dynamic action resolution")
public class HereConditionMapper extends PreConditionMapper<HereConditionData, HereCondition> {

    private final AdventureConfig adventureConfig;

    public HereConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(HereConditionData.class, HereCondition.class, this);
    }

    @Override
    public HereCondition mapToBO(HereConditionData aHereConditionData) {
        final Item item = adventureConfig.allItems().get(aHereConditionData.getThingId());
        HereCondition result = new HereCondition(item);
        result.setId(aHereConditionData.getId());
        return result;
    }

    @Override
    public HereConditionData mapToDO(HereCondition aHereCondition) {
        HereConditionData result = new HereConditionData();
        result.setThingId(aHereCondition.getThing().getId());
        result.setId(aHereCondition.getId());
        return result;
    }
}
