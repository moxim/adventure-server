package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.server.action.IncrementVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@Service
@AutoRegisterMapper(priority = 30, description = "Increment variable action mapper")
public class IncrementVariableActionMapper extends ActionMapper<IncrementVariableActionData, IncrementVariableAction> {

    private final VariableProvider variableProvider;
    private final MessagesHolder messagesHolder;

    public IncrementVariableActionMapper(@Qualifier("variableProvider") VariableProvider aVariableProvider,
                                          MessagesHolder aMessagesHolder, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        variableProvider = aVariableProvider;
        messagesHolder = aMessagesHolder;
    }

    @Override
    public IncrementVariableAction mapToBO(IncrementVariableActionData from) {
        final var action = new IncrementVariableAction(from.getName(), from.getValue(),
                                                                        variableProvider, messagesHolder);
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
