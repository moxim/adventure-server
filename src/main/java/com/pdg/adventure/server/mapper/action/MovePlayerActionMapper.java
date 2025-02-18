package com.pdg.adventure.server.mapper.action;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
public class MovePlayerActionMapper implements Mapper<MovePlayerActionData, MovePlayerAction> {

    private final MapperSupporter mapperSupporter;
    private MessagesHolder messagesHolder;

    public MovePlayerActionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
        messagesHolder = mapperSupporter.getMessagesHolder();
        mapperSupporter.registerMapper(MovePlayerActionData.class, MovePlayerAction.class, this);
    }

    @Override
    public MovePlayerAction mapToBO(MovePlayerActionData from) {
        final Location location = mapperSupporter.getMappedLocation(from.getLocationId());
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
