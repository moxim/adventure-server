package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.DiagnoseVariableActionData;
import com.pdg.adventure.server.action.DiagnoseVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Diagnose variable action mapper")
public class DiagnoseVariableActionMapper extends ActionMapper<DiagnoseVariableActionData, DiagnoseVariableAction> {

    public DiagnoseVariableActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public DiagnoseVariableAction mapToBO(DiagnoseVariableActionData from) {
        final var action = new DiagnoseVariableAction(from.getName(), getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public DiagnoseVariableActionData mapToDO(DiagnoseVariableAction from) {
        DiagnoseVariableActionData actionData = new DiagnoseVariableActionData();
        actionData.setName(from.getName());
        actionData.setId(from.getId());
        return actionData;
    }
}
