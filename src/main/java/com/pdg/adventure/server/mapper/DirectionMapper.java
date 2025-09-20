package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 65, description = "Direction mapping with location references")
public class DirectionMapper implements Mapper<DirectionData, GenericDirection> {

    private final MapperSupporter mapperSupporter;
    private Mapper<LocationData, Location> locationMapper;
    private Mapper<CommandData, Command> commandMapper;

    public DirectionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void initializeDependencies() {
        locationMapper = mapperSupporter.getMapper(LocationData.class);
        commandMapper = mapperSupporter.getMapper(CommandData.class);
    }

    @Override
    public GenericDirection mapToBO(DirectionData aDirectionData) {
        Command command = commandMapper.mapToBO(aDirectionData.getCommandData());
        boolean mustMentionDestination = aDirectionData.isDestinationMustBeMentioned();
        String locationId = aDirectionData.getDestinationId();
        GenericDirection direction = new GenericDirection(mapperSupporter.getMappedLocations(), command, locationId, mustMentionDestination);
        direction.setId(aDirectionData.getId());
        return direction;
    }

    @Override
    public DirectionData mapToDO(GenericDirection aDirection) {
        DirectionData directionData = new DirectionData();
        directionData.setId(aDirection.getId());
        String destinationId = aDirection.getDestinationId();
        directionData.setDestinationId(destinationId);
        directionData.setDestinationMustBeMentioned(aDirection.isDestinationMustBeMentioned());
        // TODO: getFirst() ? Really?
        directionData.setCommandData(commandMapper.mapToDO(aDirection.getCommands().getFirst()));
        return directionData;
    }

}
