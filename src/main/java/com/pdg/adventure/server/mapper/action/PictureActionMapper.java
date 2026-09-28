package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.server.action.PictureAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Picture action mapper")
public class PictureActionMapper extends ActionMapper<PictureActionData, PictureAction> {

    private final GameContext gameContext;

    public PictureActionMapper(GameContext aGameContext, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PictureAction mapToBO(PictureActionData actionData) {
        return new PictureAction(actionData.getPictureId(), gameContext);
    }

    @Override
    public PictureActionData mapToDO(PictureAction action) {
        PictureActionData data = new PictureActionData();
        data.setPictureId(action.getPictureId());
        return data;
    }
}
