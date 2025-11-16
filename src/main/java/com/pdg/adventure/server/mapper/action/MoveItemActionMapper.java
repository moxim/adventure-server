package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.MoveItemAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 30, description = "Move item action mapper")
public class MoveItemActionMapper extends ActionMapper<MoveItemActionData, MoveItemAction> {
    private final AdventureConfig adventureConfig;

    @Autowired
    public MoveItemActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        this.adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(MoveItemActionData.class, MoveItemAction.class, this);
    }

    @Override
    public MoveItemActionData mapToDO(MoveItemAction action) {
        return new MoveItemActionData(
                action.getTarget().getId(),
                action.getDestination().getId());
    }

    @Override
    public MoveItemAction mapToBO(MoveItemActionData actionData) {
        Item item = adventureConfig.allItems().get(actionData.getThingId());
        Container container = adventureConfig.allContainers().get(actionData.getDestinationId());
        return new MoveItemAction(item, container, adventureConfig.allMessages());
    }
}
