package com.pdg.adventure.server.mapper.action;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.server.action.SetVariableAction;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@Service
public class SetVariableActionMapper implements Mapper<SetVariableActionData, SetVariableAction> {

    private final MapperSupporter mapperSupporter;
    private VariableProvider variableProvider;
    private MessagesHolder messagesHolder;

    public SetVariableActionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
        variableProvider = mapperSupporter.getVariableProvider();
        messagesHolder = mapperSupporter.getMessagesHolder();

        mapperSupporter.registerMapper(SetVariableActionData.class, SetVariableAction.class, this);
    }

    @Override
    public SetVariableAction mapToBO(SetVariableActionData from) {
        SetVariableAction setVariableAction = new SetVariableAction(from.getVariableName(), from.getVariableValue(), variableProvider, messagesHolder);
        return setVariableAction;
    }

    @Override
    public SetVariableActionData mapToDO(SetVariableAction from) {
        SetVariableActionData setVariableActionData = new SetVariableActionData(from.getVariableName(), from.getVariableValue());
        return setVariableActionData;
    }
}
