package com.pdg.adventure.server.mapper.action;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Move player action mapper")
public class MovePlayerActionMapper extends ActionMapper<MovePlayerActionData, MovePlayerAction> {

    private MessagesHolder messagesHolder;

    public MovePlayerActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(MovePlayerActionData.class, MovePlayerAction.class, this);
    }

    @PostConstruct
    public void initializeDependencies() {
        messagesHolder = getMapperSupporter().getMessagesHolder();
    }

    @Override
    public MovePlayerAction mapToBO(MovePlayerActionData from) {
        final Location location = getMapperSupporter().getMappedLocation(from.getLocationId());
        MovePlayerAction movePlayerAction = new MovePlayerAction(location, messagesHolder);
        return movePlayerAction;
    }

    @Override
    public MovePlayerActionData mapToDO(MovePlayerAction from) {
        MovePlayerActionData movePlayerActionData = new MovePlayerActionData();
        movePlayerActionData.setLocationId(from.getDestination().getId());
        return movePlayerActionData;
    }
}
