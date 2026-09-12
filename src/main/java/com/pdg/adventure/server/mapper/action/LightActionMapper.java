package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.LightActionData;
import com.pdg.adventure.server.action.LightAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 30, description = "Light action mapper")
public class LightActionMapper extends ActionMapper<LightActionData, LightAction> {

    public LightActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public LightAction mapToBO(LightActionData actionData) {
        Item item = getMapperSupporter().requireMappedItem(actionData.getThingId(), actionData);
        return new LightAction(item, actionData.getLumen());
    }

    @Override
    public LightActionData mapToDO(LightAction action) {
        LightActionData data = new LightActionData();
        data.setThingId(action.getItem().getId());
        data.setLumen(action.getLumen());
        return data;
    }
}
