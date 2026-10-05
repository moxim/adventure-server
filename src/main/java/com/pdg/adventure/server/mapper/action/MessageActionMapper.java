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
        // TODO: Review needed — messageId is now the message's id, but a MessageAction only holds the
        //  resolved text, so this stores the text instead. No production code maps BO -> DO (editors
        //  write the data objects directly); reverse-lookup the id (or keep it in MessageAction)
        //  before this path is ever used to save.
        data.setMessageId(action.getMessage());
        return data;
    }

    @Override
    public MessageAction mapToBO(MessageActionData actionData) {
        String message = adventureConfig.allMessages().getMessage(actionData.getMessageId());
        if (message == null) {
            // TODO: Review needed — fallback keeps literal-text references working (a messageId that matches
            //  no message is used as the text itself); undecided whether to keep it now that references are ids.
            message = actionData.getMessageId();
        }
        return new MessageAction(message, adventureConfig.allMessages().getFont(actionData.getMessageId()));
    }
}
