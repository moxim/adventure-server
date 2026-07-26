package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.server.action.IncrementVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Increment variable action mapper")
public class IncrementVariableActionMapper extends ActionMapper<IncrementVariableActionData, IncrementVariableAction> {

    private final MessagesHolder messagesHolder;

    public IncrementVariableActionMapper(MessagesHolder aMessagesHolder, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        messagesHolder = aMessagesHolder;
    }

    @Override
    public IncrementVariableAction mapToBO(IncrementVariableActionData from) {
        final var action = new IncrementVariableAction(from.getName(), from.getValue(),
                                                                        getMapperSupporter().getVariableProvider(), messagesHolder);
        action.setId(from.getId());
        return action;
    }

    @Override
    public IncrementVariableActionData mapToDO(IncrementVariableAction from) {
        IncrementVariableActionData actionData = new IncrementVariableActionData();
        actionData.setName(from.getName());
        actionData.setValue(from.getValue());
        actionData.setId(from.getId());
        return actionData;
    }
}
