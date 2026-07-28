package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.BreakActionData;
import com.pdg.adventure.server.action.BreakAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Break action mapper")
public class BreakActionMapper extends ActionMapper<BreakActionData, BreakAction> {

    private final MessagesHolder messagesHolder;

    public BreakActionMapper(MapperSupporter aMapperSupporter, MessagesHolder aMessagesHolder) {
        super(aMapperSupporter);
        messagesHolder = aMessagesHolder;
    }

    @Override
    public BreakAction mapToBO(BreakActionData data) {
        return new BreakAction(messagesHolder);
    }

    @Override
    public BreakActionData mapToDO(BreakAction action) {
        return new BreakActionData();
    }
}
