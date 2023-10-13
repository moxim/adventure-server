package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationMapper implements Mapper<LocationData, Location> {

    private MapperProvider mapperProvider;

    @Autowired
    public LocationMapper(MapperProvider aMapperProvider) {
        mapperProvider = aMapperProvider;
    }

    public Location mapToBO(LocationData aLocationData) {
        DescriptionProvider descriptionProvider = DescriptionMapper.mapToBO(aLocationData.getDescriptionData());
        ItemContainerMapper directionContainerMapper = mapperProvider.getMapper(ItemContainerMapper.class);
        Container pocket = directionContainerMapper.mapToBO(aLocationData.getItemContainerData());
        Location location = new Location(descriptionProvider, pocket);
        location.setId(aLocationData.getId());
        location.setLight(aLocationData.getLumen());
        for (Containable item : pocket.getContents()) {
            location.addItem(item);
        }
        return location;
    }

    public LocationData mapToDO(Location aLoction) {
        LocationData result = new LocationData();
        result.setId(aLoction.getId());
        result.setLumen(aLoction.getLight());
        // TODO: fill in rest
        return result;
    }
}
