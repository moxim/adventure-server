package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.MoveItemAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 30, description = "Move item action mapper")
public class MoveItemActionMapper extends ActionMapper<MoveItemActionData, MoveItemAction> {
    private final MessagesHolder messagesHolder;
    private final Map<String, Item> allItems;
    private final Map<String, Container> allContainers;

    @Autowired
    public MoveItemActionMapper(MapperSupporter aMapperSupporter, AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        messagesHolder = anAdventureConfig.allMessages();
        allItems = anAdventureConfig.allItems();
        allContainers = anAdventureConfig.allContainers();
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
        Item item = allItems.get(actionData.getThingId());
        Container container = allContainers.get(actionData.getDestinationId());
        return new MoveItemAction(item, container, messagesHolder);
    }
}
