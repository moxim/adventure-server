package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.PrintVariablesActionData;
import com.pdg.adventure.server.action.PrintVariablesAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Print variables action mapper")
public class PrintVariablesActionMapper extends ActionMapper<PrintVariablesActionData, PrintVariablesAction> {

    private final GameContext gameContext;

    public PrintVariablesActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PrintVariablesAction mapToBO(PrintVariablesActionData from) {
        final var action = new PrintVariablesAction(gameContext::tell, getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public PrintVariablesActionData mapToDO(PrintVariablesAction from) {
        PrintVariablesActionData actionData = new PrintVariablesActionData();
        actionData.setId(from.getId());
        return actionData;
    }
}
