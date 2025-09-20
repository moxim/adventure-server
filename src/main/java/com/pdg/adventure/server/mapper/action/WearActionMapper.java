package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.WearActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.WearAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 30, description = "Wear action mapper")
public class WearActionMapper implements Mapper<WearActionData, WearAction> {
    private final MessagesHolder messagesHolder;
    private final Map<String, Item> allItems;

    @Autowired
    public WearActionMapper(AdventureConfig anAdventureConfig) {
        messagesHolder = anAdventureConfig.allMessages();
        allItems = anAdventureConfig.allItems();
    }

    @Override
    public WearActionData mapToDO(WearAction action) {
        WearActionData data = new WearActionData();
        data.setThingId(action.getThing().getId());
        return data;
    }

    @Override
    public WearAction mapToBO(WearActionData actionData) {
        return new WearAction(allItems.get(actionData.getThingId()), messagesHolder);
    }
}
