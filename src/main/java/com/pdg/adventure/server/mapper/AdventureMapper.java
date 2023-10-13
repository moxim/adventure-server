package com.pdg.adventure.server.mapper;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.MapperProvider;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
public class AdventureMapper { // extends Mapper<AdventureData, Adventure>{

    private final MapperProvider mapperProvider;

    public AdventureMapper(@Autowired MapperProvider aMapperProvider) {
        mapperProvider = aMapperProvider;
    }

    public Adventure mapToBO(AdventureData anAdventureData) {
        Adventure adventure = new Adventure(new Vocabulary(), new HashMap<>(4), new MessagesHolder(), new HashMap<>());
        adventure.setId((anAdventureData.getId()));
        adventure.setTitle(anAdventureData.getTitle());
        LocationMapper locationMapper = mapperProvider.getMapper(LocationMapper.class);
        Set<LocationData> locationDataList = anAdventureData.getLocationData();
        List<Location> locationList = locationMapper.mapToBOs(List.copyOf(locationDataList));
        adventure.setLocations(locationList);
        adventure.setCurrentLocationId(anAdventureData.getCurrentLocationId());
        ItemContainerMapper containerMapper = mapperProvider.getMapper(ItemContainerMapper.class);
        adventure.setPocket(containerMapper.mapToBO(anAdventureData.getPlayerPocket()));
//        DirectionContainerMapper directionContainerMapper = mapperProvider.getMapper(DirectionContainerMapper.class);
//        adventure.setPocket(directionContainerMapper.mapToBO(anAdventureData.getPlayerPocket()));
        return adventure;
    }

    public AdventureData mapToDO(Adventure anAdventure) {
        AdventureData adventureData = new AdventureData();
        adventureData.setId(anAdventure.getId());
        adventureData.setTitle(anAdventure.getTitle());
        adventureData.setCurrentLocationId(anAdventure.getCurrentLocationId());
        LocationMapper locationMapper = mapperProvider.getMapper(LocationMapper.class);
        List<LocationData> locationDataList = locationMapper.mapToDOs(anAdventure.getLocations());
        adventureData.setLocationData(Set.copyOf(locationDataList));
        ItemContainerMapper containerMapper = mapperProvider.getMapper(ItemContainerMapper.class);
        adventureData.setPlayerPocket(containerMapper.mapToDO(anAdventure.getPocket()));
//        DirectionContainerMapper directionContainerMapper = mapperProvider.getMapper(DirectionContainerMapper.class);
//        adventureData.setPlayerPocket(directionContainerMapper.mapToDO(anAdventure.getPocket()));
        return adventureData;
    }
}
