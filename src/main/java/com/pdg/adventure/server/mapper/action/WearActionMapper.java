package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.WearActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.WearAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Wear action mapper")
public class WearActionMapper extends ActionMapper<WearActionData, WearAction> {
    private final AdventureConfig adventureConfig;

    @Autowired
    public WearActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        this.adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(WearActionData.class, WearAction.class, this);
    }

    @Override
    public WearActionData mapToDO(WearAction action) {
        WearActionData data = new WearActionData();
        data.setThingId(action.getThing().getId());
        return data;
    }

    @Override
    public WearAction mapToBO(WearActionData actionData) {
        return new WearAction(
                adventureConfig.allItems().get(actionData.getThingId()),
                adventureConfig.allMessages());
    }
}
