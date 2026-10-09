package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.ExamineActionData;
import com.pdg.adventure.server.action.ExamineAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Examine action mapper")
public class ExamineActionMapper extends ActionMapper<ExamineActionData, ExamineAction> {

    private final GameContext gameContext;

    public ExamineActionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public ExamineAction mapToBO(ExamineActionData data) {
        return new ExamineAction(gameContext);
    }

    @Override
    public ExamineActionData mapToDO(ExamineAction action) {
        return new ExamineActionData();
    }
}
