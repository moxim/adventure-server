package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.PrintVariableActionData;
import com.pdg.adventure.server.action.PrintVariableAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Print variable action mapper")
public class PrintVariableActionMapper extends ActionMapper<PrintVariableActionData, PrintVariableAction> {

    private final GameContext gameContext;

    public PrintVariableActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PrintVariableAction mapToBO(PrintVariableActionData from) {
        final var action = new PrintVariableAction(from.getName(), gameContext::tell,
                                                   getMapperSupporter().getVariableProvider());
        action.setId(from.getId());
        return action;
    }

    @Override
    public PrintVariableActionData mapToDO(PrintVariableAction from) {
        PrintVariableActionData actionData = new PrintVariableActionData();
        actionData.setName(from.getName());
        actionData.setId(from.getId());
        return actionData;
    }
}
