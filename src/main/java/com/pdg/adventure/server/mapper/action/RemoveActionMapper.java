package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.RemoveAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Remove action mapper")
public class RemoveActionMapper extends ActionMapper<RemoveActionData, RemoveAction> {

    private final AdventureConfig adventureConfig;

    public RemoveActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
    }

    @Override
    public RemoveAction mapToBO(RemoveActionData actionData) {
        return new RemoveAction(
                adventureConfig.allItems().get(actionData.getThingId()),
                adventureConfig.allMessages());
    }

    @Override
    public RemoveActionData mapToDO(RemoveAction action) {
        RemoveActionData data = new RemoveActionData();
        data.setThingId(action.getThing().getId());
        return data;
    }
}
