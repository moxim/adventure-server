package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LocationMapper implements Mapper<LocationData, Location> {

    private final MapperSupporter mapperSupporter;

    public LocationMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    public Location mapToBO(LocationData aLocationData) {
        Location mappedLocation = mapperSupporter.getMappedLocation(aLocationData.getId());
        if (mappedLocation != null) {
            return mappedLocation;
        }
        DescriptionMapper descriptionMapper = mapperSupporter.getMapper(DescriptionMapper.class);
        DescriptionProvider descriptionProvider = descriptionMapper.mapToBO(aLocationData.getDescriptionData());
        if (descriptionProvider == null) {
            throw new IllegalArgumentException("DescriptionProvider is null");
        }
        ItemContainerMapper directionContainerMapper = mapperSupporter.getMapper(ItemContainerMapper.class);

        final ItemContainerData itemContainerData = aLocationData.getItemContainerData();
        ItemContainerMapper itemContainerMapper = mapperSupporter.getMapper(ItemContainerMapper.class);
        final GenericContainer itemContainer = itemContainerMapper.mapToBO(itemContainerData);

        Location location = new Location(descriptionProvider, itemContainer);
        location.setId(aLocationData.getId());
        mapperSupporter.addMappedLocation(location);

        location.setLight(aLocationData.getLumen());
        // TODO: change in LocationData to be an Integer
        location.setTimesVisited(aLocationData.isHasBeenVisited() ? 1 : 0);

        final Set<DirectionData> directionsData = aLocationData.getDirectionsData();
        final DirectionMapper directionMapper = mapperSupporter.getMapper(DirectionMapper.class);
        for (DirectionData directionData : directionsData) {
            final GenericDirection direction = directionMapper.mapToBO(directionData);
            location.addDirection(direction);
        }

        final CommandProviderData commandProviderData = aLocationData.getCommandProviderData();
        final CommandProviderMapper commandProviderMapper = mapperSupporter.getMapper(CommandProviderMapper.class);
        final CommandProvider commandProvider = commandProviderMapper.mapToBO(commandProviderData);
        location.setCommandProvider(commandProvider);

        return location;
    }

    public LocationData mapToDO(Location aLoction) {
        LocationData result = new LocationData();
        result.setId(aLoction.getId());
        result.setLumen(aLoction.getLight());
        DescriptionMapper descriptionMapper = mapperSupporter.getMapper(DescriptionMapper.class);
        result.setDescriptionData(descriptionMapper.mapToDO(aLoction.getDescriptionProvider()));
        ItemContainerMapper itemContainerMapper = mapperSupporter.getMapper(ItemContainerMapper.class);
        result.setItemContainerData(itemContainerMapper.mapToDO(aLoction.getContainer()));
        DirectionMapper directionMapper = mapperSupporter.getMapper(DirectionMapper.class);
        for (DirectionData directionData : directionMapper.mapToDOs(aLoction.getDirections())) {
            result.getDirectionsData().add(directionData);
        }
        return result;
    }
}
