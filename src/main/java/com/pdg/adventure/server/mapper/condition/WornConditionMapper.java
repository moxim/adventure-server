package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "WornCondition mapper")
public class WornConditionMapper extends PreConditionMapper<WornConditionData, WornCondition> {

    public WornConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public WornCondition mapToBO(WornConditionData data) {
        final Item item = getMapperSupporter().requireMappedItem(data.getThingId(), data);
        WornCondition result = new WornCondition(item);
        result.setId(data.getId());
        return result;
    }

    @Override
    public WornConditionData mapToDO(WornCondition condition) {
        WornConditionData result = new WornConditionData();
        result.setThingId(condition.getThing().getId());
        result.setId(condition.getId());
        return result;
    }
}
