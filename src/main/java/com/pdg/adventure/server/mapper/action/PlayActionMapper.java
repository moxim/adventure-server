package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.PlayActionData;
import com.pdg.adventure.server.action.PlayAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Play action mapper")
public class PlayActionMapper extends ActionMapper<PlayActionData, PlayAction> {

    private final GameContext gameContext;

    public PlayActionMapper(GameContext aGameContext, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PlayAction mapToBO(PlayActionData actionData) {
        return new PlayAction(actionData.getSoundId(), actionData.getTimes(), gameContext);
    }

    @Override
    public PlayActionData mapToDO(PlayAction action) {
        PlayActionData data = new PlayActionData();
        data.setSoundId(action.getSoundId());
        data.setTimes(action.getTimes());
        return data;
    }
}
