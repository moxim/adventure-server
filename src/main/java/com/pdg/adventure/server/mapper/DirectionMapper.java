package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import org.springframework.stereotype.Service;

@Service
public class DirectionMapper implements Mapper<DirectionData, GenericDirection> {

    private final MapperSupporter mapperSupporter;

//    @Autowired
    public DirectionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }


    @Override
    public GenericDirection mapToBO(DirectionData aDirectionData) {
        final CommandMapper commandMapper = mapperSupporter.getMapper(CommandMapper.class);
        Command command = commandMapper.mapToBO(aDirectionData.getCommandData());
        boolean mustMentionDestination = aDirectionData.isDestinationMustBeMentioned();
        LocationMapper locMapper = mapperSupporter.getMapper(LocationMapper.class);
        LocationData destinationData = aDirectionData.getDestinationData();
        String locationId = destinationData.getId();
        Location destination;
//        if (mapperSupporter.getMappedLocation(locationId) != null) {
//            destination = mapperSupporter.getMappedLocation(locationId);
//            return new GenericDirection(command, destination, mustMentionDestination);
//        }
        destination = locMapper.mapToBO(destinationData);

        GenericDirection direction = new GenericDirection(command, destination, mustMentionDestination);
        direction.setId(aDirectionData.getId());
        return direction;
    }

    @Override
    public DirectionData mapToDO(GenericDirection aDirection) {
        DirectionData directionData = new DirectionData();
        directionData.setId(aDirection.getId());
        LocationMapper locMapper = mapperSupporter.getMapper(LocationMapper.class);
        LocationData destination = locMapper.mapToDO(aDirection.getDestination());
        directionData.setDestinationData(destination);
        directionData.setDestinationMustBeMentioned(aDirection.isDestinationMustBeMentioned());
        final CommandMapper commandMapper = mapperSupporter.getMapper(CommandMapper.class);
        // TODO: getFirst() ? Really?
        directionData.setCommandData(commandMapper.mapToDO(aDirection.getCommands().getFirst()));
        return directionData;
    }

}
