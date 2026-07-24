package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.ItemAtCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "ItemAtCondition mapper")
public class ItemAtConditionMapper extends PreConditionMapper<ItemAtConditionData, ItemAtCondition> {

    public ItemAtConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public ItemAtCondition mapToBO(ItemAtConditionData data) {
        final Item item = getMapperSupporter().requireMappedItem(data.getThingId(), data);
        final Location location = getMapperSupporter().requireMappedLocation(data.getLocationId(), data);
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
