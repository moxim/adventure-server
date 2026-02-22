package com.pdg.adventure.server.mapper;

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
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;

@Service
@AutoRegisterMapper(priority = 60, description = "Location mapping with complex dependencies")
public class LocationMapper implements Mapper<LocationData, Location> {

    private final MapperSupporter mapperSupporter;
    private final Mapper<DescriptionData, DescriptionProvider> descriptionMapper;
    private final Mapper<ItemContainerData, GenericContainer> itemContainerMapper;
    private final Mapper<DirectionData, GenericDirection> directionMapper;
    private final Mapper<CommandProviderData, GenericCommandProvider> commandProviderMapper;

    public LocationMapper(MapperSupporter aMapperSupporter,
                          DescriptionMapper aDescriptionMapper,
                          ItemContainerMapper aItemContainerMapper,
                          DirectionMapper aDirectionMapper,
                          CommandProviderMapper aCommandProviderMapper) {
        mapperSupporter = aMapperSupporter;
        descriptionMapper = aDescriptionMapper;
        itemContainerMapper = aItemContainerMapper;
        directionMapper = aDirectionMapper;
        commandProviderMapper = aCommandProviderMapper;
        mapperSupporter.registerMapper(LocationData.class, Location.class, this);
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

        Location location = new Location(descriptionProvider);
        location.setId(aLocationData.getId());
        mapperSupporter.addMappedLocation(location);

        location.setLight(aLocationData.getLumen());
        location.setTimesVisited(aLocationData.getTimesVisited());
        return location;
    }

    public LocationData mapToDO(Location aLocation) {
        LocationData result = new LocationData();
        result.setId(aLocation.getId());
        result.setLumen(aLocation.getLight());
        result.setDescriptionData(descriptionMapper.mapToDO(aLocation.getDescriptionProvider()));
        result.setItemContainerData(itemContainerMapper.mapToDO(aLocation.getItemContainer()));
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
            Location location = mapperSupporter.getMappedLocation(locationData.getId());
            mapLocationDirections(locationData, location);
            mapLocationItems(locationData, location);
            mapLocationCommands(locationData, location);
        }

        return result;
    }

    private void mapLocationItems(final LocationData aLocationData, final Location aLocation) {
        final ItemContainerData itemContainerData = aLocationData.getItemContainerData();
        final Container itemContainer = itemContainerMapper.mapToBO(itemContainerData);
        aLocation.setItemContainer(itemContainer);
    }

    private void mapLocationCommands(final LocationData locationData, final Location location) {
        final CommandProviderData commandProviderData = locationData.getCommandProviderData();
        final GenericCommandProvider commandProvider = commandProviderMapper.mapToBO(commandProviderData);
        location.setCommandProvider(commandProvider);
    }

    private void mapLocationDirections(final LocationData locationData, final Location location) {
        final Set<DirectionData> directionsData = locationData.getDirectionsData();
        for (DirectionData directionData : directionsData) {
            final GenericDirection direction = directionMapper.mapToBO(directionData);
            location.addDirection(direction);
        }
    }

    public List<LocationData> mapToDOs(List<Location> aBusinessObjectList) {
        List<LocationData> result = new ArrayList<>(aBusinessObjectList.size());
        for (Location location : aBusinessObjectList) {
            result.add(mapToDO(location));
        }

        for (Location location : aBusinessObjectList) {
            LocationData locationData;
            for (DirectionData _ : directionMapper.mapToDOs(location.getDirections())) {
                // TODO: find locationData, maybe by using a local Map<>
                // locationData.add(directionData);
            }
        }
        return result;
    }
}
