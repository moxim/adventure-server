package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Move player action mapper")
public class MovePlayerActionMapper extends ActionMapper<MovePlayerActionData, MovePlayerAction> {

    private final GameContext gameContext;

    public MovePlayerActionMapper(GameContext aGameContext,
                                  MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public MovePlayerAction mapToBO(MovePlayerActionData from) {
        final Location location = getMapperSupporter().getMappedLocation(from.getLocationId());
        MovePlayerAction movePlayerAction = new MovePlayerAction(location, gameContext);
        return movePlayerAction;
    }

    @Override
    public MovePlayerActionData mapToDO(MovePlayerAction from) {
        MovePlayerActionData movePlayerActionData = new MovePlayerActionData();
        movePlayerActionData.setLocationId(from.getDestination().getId());
        return movePlayerActionData;
    }
}
