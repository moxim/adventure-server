package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.server.action.DecrementVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@Service
@AutoRegisterMapper(priority = 30, description = "Decrement variable action mapper")
public class DecrementVariableActionMapper extends ActionMapper<DecrementVariableActionData, DecrementVariableAction> {

    private final VariableProvider variableProvider;
    private final MessagesHolder messagesHolder;

    public DecrementVariableActionMapper(@Qualifier("variableProvider") VariableProvider aVariableProvider,
                                          MessagesHolder aMessagesHolder, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        variableProvider = aVariableProvider;
        messagesHolder = aMessagesHolder;
    }

    @Override
    public DecrementVariableAction mapToBO(DecrementVariableActionData from) {
        return new DecrementVariableAction(from.getName(), from.getValue(), variableProvider, messagesHolder);
    }

    @Override
    public DecrementVariableActionData mapToDO(DecrementVariableAction from) {
        return new DecrementVariableActionData(from.getName(), from.getValue());
    }
}
