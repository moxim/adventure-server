package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Message action mapper")
public class MessageActionMapper extends ActionMapper<MessageActionData, MessageAction> {
    private final AdventureConfig adventureConfig;

    public MessageActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        this.adventureConfig = anAdventureConfig;
    }

    @Override
    public MessageActionData mapToDO(MessageAction action) {
        MessageActionData data = new MessageActionData();
        data.setMessageId(action.getMessage());
        return data;
    }

    @Override
    public MessageAction mapToBO(MessageActionData actionData) {
        String message = adventureConfig.allMessages().getMessage(actionData.getMessageId());
        if (message == null) {
            message = actionData.getMessageId();
        }
        return new MessageAction(message, adventureConfig.allMessages().getFont(actionData.getMessageId()));
    }
}
