package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.DiagnoseVariablesActionData;
import com.pdg.adventure.server.action.DiagnoseVariablesAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Diagnose variables action mapper")
public class DiagnoseVariablesActionMapper extends ActionMapper<DiagnoseVariablesActionData, DiagnoseVariablesAction> {

    public DiagnoseVariablesActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public DiagnoseVariablesAction mapToBO(DiagnoseVariablesActionData from) {
        final var action = new DiagnoseVariablesAction(getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public DiagnoseVariablesActionData mapToDO(DiagnoseVariablesAction from) {
        DiagnoseVariablesActionData actionData = new DiagnoseVariablesActionData();
        actionData.setId(from.getId());
        return actionData;
    }
}
