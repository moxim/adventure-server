package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.server.action.DecrementVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Decrement variable action mapper")
public class DecrementVariableActionMapper extends ActionMapper<DecrementVariableActionData, DecrementVariableAction> {

    public DecrementVariableActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public DecrementVariableAction mapToBO(DecrementVariableActionData from) {
        return new DecrementVariableAction(from.getName(), from.getValue(), getMapperSupporter().getVariableProvider());
    }

    @Override
    public DecrementVariableActionData mapToDO(DecrementVariableAction from) {
        return new DecrementVariableActionData(from.getName(), from.getValue());
    }
}
