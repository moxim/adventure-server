package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.AddVariableActionData;
import com.pdg.adventure.server.action.AddVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Add variable action mapper")
public class AddVariableActionMapper extends ActionMapper<AddVariableActionData, AddVariableAction> {

    public AddVariableActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public AddVariableAction mapToBO(AddVariableActionData from) {
        final var action = new AddVariableAction(from.getFirstName(), from.getSecondName(),
                                                  getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public AddVariableActionData mapToDO(AddVariableAction from) {
        AddVariableActionData actionData = new AddVariableActionData();
        actionData.setFirstName(from.getFirstName());
        actionData.setSecondName(from.getSecondName());
        actionData.setId(from.getId());
        return actionData;
    }
}
