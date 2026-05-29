package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "WornCondition mapper")
public class WornConditionMapper extends PreConditionMapper<WornConditionData, WornCondition> {

    private final AdventureConfig adventureConfig;

    public WornConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(WornConditionData.class, WornCondition.class, this);
    }

    @Override
    public WornCondition mapToBO(WornConditionData data) {
        final Item item = adventureConfig.allItems().get(data.getThingId());
        WornCondition result = new WornCondition(item);
        result.setId(data.getId());
        return result;
    }

    @Override
    public WornConditionData mapToDO(WornCondition condition) {
        WornConditionData result = new WornConditionData();
        result.setThingId(((Item) condition.getThing()).getId());
        result.setId(condition.getId());
        return result;
    }
}
