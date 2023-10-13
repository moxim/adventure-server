package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DirectionMapper implements Mapper<DirectionData, GenericDirection> {

    private final MapperProvider mapperProvider;

    @Autowired
    public DirectionMapper(MapperProvider aMapperProvider) {
        mapperProvider = aMapperProvider;
    }


    @Override
    public GenericDirection mapToBO(DirectionData aDirectionData) {
        Command command = CommandMapper.mapToBO(aDirectionData.getCommandData());
        boolean mustMentionDestination = aDirectionData.isDestinationMustBeMentioned();
        LocationMapper locMapper = mapperProvider.getMapper(LocationMapper.class);
        Location destination = locMapper.mapToBO(aDirectionData.getDestinationData());
        GenericDirection direction = new GenericDirection(command, destination, mustMentionDestination);
        direction.setId(aDirectionData.getId());
        return direction;
    }

    @Override
    public DirectionData mapToDO(GenericDirection aDirection) {
        DirectionData directionData = new DirectionData();
        directionData.setId(aDirection.getId());
        LocationMapper locMapper = mapperProvider.getMapper(LocationMapper.class);
        LocationData destination = locMapper.mapToDO(aDirection.getDestination());
        directionData.setDestinationData(destination);
        directionData.setDestinationMustBeMentioned(aDirection.isDestinationMustBeMentioned());
        // TODO get(0) ? Really?
        directionData.setCommandData(CommandMapper.mapToBO(aDirection.getCommands().get(0)));
        return directionData;
    }

}
