package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;

@Service
@AutoRegisterMapper(priority = 60, description = "Location mapping with complex dependencies")
public class LocationMapper implements Mapper<LocationData, Location> {

    private final MapperSupporter mapperSupporter;
    private Mapper<DescriptionData, DescriptionProvider> descriptionMapper;
    private Mapper<ItemContainerData, GenericContainer> itemContainerMapper;
    private Mapper<DirectionData, GenericDirection> directionMapper;
    private Mapper<CommandProviderData, CommandProvider> commandProviderMapper;

    public LocationMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void initializeDependencies() {
        descriptionMapper = mapperSupporter.getMapper(DescriptionData.class);
        itemContainerMapper = mapperSupporter.getMapper(ItemContainerData.class);
        directionMapper = mapperSupporter.getMapper(DirectionData.class);
        commandProviderMapper = mapperSupporter.getMapper(CommandProviderData.class);
    }

    public Location mapToBO(LocationData aLocationData) {
        Location mappedLocation = mapperSupporter.getMappedLocation(aLocationData.getId());
        if (mappedLocation != null) {
            return mappedLocation;
        }
        DescriptionProvider descriptionProvider = descriptionMapper.mapToBO(aLocationData.getDescriptionData());
        if (descriptionProvider == null) {
            throw new IllegalArgumentException("DescriptionProvider is null");
        }

        final ItemContainerData itemContainerData = aLocationData.getItemContainerData();
        final Container itemContainer = itemContainerMapper.mapToBO(itemContainerData);

        Location location = new Location(descriptionProvider, itemContainer);
        location.setId(aLocationData.getId());
        mapperSupporter.addMappedLocation(location);

        location.setLight(aLocationData.getLumen());
        // TODO: change in LocationData to be an Integer
        location.setTimesVisited(aLocationData.isHasBeenVisited() ? 1 : 0);

//        final Set<DirectionData> directionsData = aLocationData.getDirectionsData();
//        for (DirectionData directionData : directionsData) {
//            final GenericDirection direction = directionMapper.mapToBO(directionData);
//            location.addDirection(direction);
//        }
//
        final CommandProviderData commandProviderData = aLocationData.getCommandProviderData();
        final CommandProvider commandProvider = commandProviderMapper.mapToBO(commandProviderData);
        location.setCommandProvider(commandProvider);

        return location;
    }

    public LocationData mapToDO(Location aLocation) {
        LocationData result = new LocationData();
        result.setId(aLocation.getId());
        result.setLumen(aLocation.getLight());
        result.setDescriptionData(descriptionMapper.mapToDO(aLocation.getDescriptionProvider()));
        result.setItemContainerData(itemContainerMapper.mapToDO(aLocation.getContainer()));
//        for (DirectionData directionData : directionMapper.mapToDOs(aLocation.getDirections())) {
//            result.getDirectionsData().add(directionData);
//        }
        return result;
    }

    public List<Location> mapToBOs(List<LocationData> aDataObjectList) {
        List<Location> result = new ArrayList<>(aDataObjectList.size());
        for (LocationData dataObject : aDataObjectList) {
            result.add(mapToBO(dataObject));
        }

        for (LocationData locationData : aDataObjectList) {
            final Set<DirectionData> directionsData = locationData.getDirectionsData();
            Location location = mapperSupporter.getMappedLocation(locationData.getId());
            for (DirectionData directionData : directionsData) {
                final GenericDirection direction = directionMapper.mapToBO(directionData);
                location.addDirection(direction);
            }
        }

        return result;
    }

    public List<LocationData> mapToDOs(List<Location> aBusinessObjectList) {
        List<LocationData> result = new ArrayList<>(aBusinessObjectList.size());
        for (Location location : aBusinessObjectList) {
            result.add(mapToDO(location));
        }

        for (Location location : aBusinessObjectList) {
            LocationData locationData;
            for (DirectionData directionData : directionMapper.mapToDOs(location.getDirections())) {
                // TODO: find locationData, maybe by using a local Map<>
                // locationData.add(directionData);
            }
        }
        return result;
    }
}
