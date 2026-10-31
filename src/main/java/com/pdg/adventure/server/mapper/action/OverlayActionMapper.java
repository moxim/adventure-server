package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.OverlayActionData;
import com.pdg.adventure.server.action.OverlayAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Overlay action mapper")
public class OverlayActionMapper extends ActionMapper<OverlayActionData, OverlayAction> {

    private final GameContext gameContext;

    public OverlayActionMapper(GameContext aGameContext, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public OverlayAction mapToBO(OverlayActionData from) {
        OverlayAction action = new OverlayAction(from.getX(), from.getY(), from.getPictureId(), gameContext);
        action.setId(from.getId());
        return action;
    }

    @Override
    public OverlayActionData mapToDO(OverlayAction from) {
        OverlayActionData data = new OverlayActionData();
        data.setX(from.getX());
        data.setY(from.getY());
        data.setPictureId(from.getPictureId());
        data.setId(from.getId());
        return data;
    }
}
