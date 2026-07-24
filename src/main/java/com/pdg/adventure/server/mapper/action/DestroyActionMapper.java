package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.DestroyAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Destroy action mapper")
public class DestroyActionMapper extends ActionMapper<DestroyActionData, DestroyAction> {

    private final AdventureConfig adventureConfig;

    public DestroyActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
    }

    @Override
    public DestroyAction mapToBO(DestroyActionData actionData) {
        return new DestroyAction(
                getMapperSupporter().requireMappedItem(actionData.getThingId(), actionData),
                adventureConfig.allMessages());
    }

    @Override
    public DestroyActionData mapToDO(DestroyAction action) {
        DestroyActionData data = new DestroyActionData();
        data.setThingId(action.getThing().getId());
        return data;
    }
}
