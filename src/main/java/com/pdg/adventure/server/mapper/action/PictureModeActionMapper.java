package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.model.action.PictureModeActionData;
import com.pdg.adventure.server.action.PictureModeAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "PictureMode action mapper")
public class PictureModeActionMapper extends ActionMapper<PictureModeActionData, PictureModeAction> {

    private final GameContext gameContext;

    public PictureModeActionMapper(GameContext aGameContext, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PictureModeAction mapToBO(PictureModeActionData from) {
        // TODO: Review needed — an action without a mode (the editor requires one) behaves like DEFAULT.
        PictureModeAction action = new PictureModeAction(from.getMode() == null ? PictureMode.DEFAULT : from.getMode(),
                                                         gameContext);
        action.setId(from.getId());
        return action;
    }

    @Override
    public PictureModeActionData mapToDO(PictureModeAction from) {
        PictureModeActionData data = new PictureModeActionData();
        data.setMode(from.getMode());
        data.setId(from.getId());
        return data;
    }
}
