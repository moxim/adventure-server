package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.PrintActionData;
import com.pdg.adventure.server.action.PrintAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Print action mapper")
public class PrintActionMapper extends ActionMapper<PrintActionData, PrintAction> {

    public PrintActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public PrintAction mapToBO(PrintActionData from) {
        final var action = new PrintAction(from.getName(), getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public PrintActionData mapToDO(PrintAction from) {
        PrintActionData actionData = new PrintActionData();
        actionData.setName(from.getName());
        actionData.setId(from.getId());
        return actionData;
    }
}
