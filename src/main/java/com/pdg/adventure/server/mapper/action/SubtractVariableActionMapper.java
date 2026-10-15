package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.SubtractVariableActionData;
import com.pdg.adventure.server.action.SubtractVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Subtract variable action mapper")
public class SubtractVariableActionMapper extends ActionMapper<SubtractVariableActionData, SubtractVariableAction> {

    public SubtractVariableActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public SubtractVariableAction mapToBO(SubtractVariableActionData from) {
        final var action = new SubtractVariableAction(from.getFirstName(), from.getSecondName(),
                                                  getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public SubtractVariableActionData mapToDO(SubtractVariableAction from) {
        SubtractVariableActionData actionData = new SubtractVariableActionData();
        actionData.setFirstName(from.getFirstName());
        actionData.setSecondName(from.getSecondName());
        actionData.setId(from.getId());
        return actionData;
    }
}
