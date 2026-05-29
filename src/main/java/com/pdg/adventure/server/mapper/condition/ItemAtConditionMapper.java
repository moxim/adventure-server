package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.ItemAtCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "ItemAtCondition mapper")
public class ItemAtConditionMapper extends PreConditionMapper<ItemAtConditionData, ItemAtCondition> {

    private final AdventureConfig adventureConfig;

    public ItemAtConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(ItemAtConditionData.class, ItemAtCondition.class, this);
    }

    @Override
    public ItemAtCondition mapToBO(ItemAtConditionData data) {
        final Item item = adventureConfig.allItems().get(data.getThingId());
        final Location location = adventureConfig.allLocations().get(data.getLocationId());
        ItemAtCondition result = new ItemAtCondition(item, location);
        result.setId(data.getId());
        return result;
    }

    @Override
    public ItemAtConditionData mapToDO(ItemAtCondition condition) {
        ItemAtConditionData result = new ItemAtConditionData();
        result.setThingId(condition.getThing().getId());
        result.setLocationId(condition.getLocation().getId());
        result.setId(condition.getId());
        return result;
    }
}
