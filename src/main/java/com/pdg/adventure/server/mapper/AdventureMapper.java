package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdventureMapper implements Mapper<AdventureData, Adventure> {

    private final MapperSupporter mapperSupporter;

    public AdventureMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    public Adventure mapToBO(AdventureData anAdventureData) {
        final VocabularyMapper vocabularyMapper = mapperSupporter.getMapper(VocabularyMapper.class);
        final Vocabulary vocabulary = vocabularyMapper.mapToBO(anAdventureData.getVocabularyData());
        Adventure adventure = new Adventure(vocabulary, new HashMap<>(4), new MessagesHolder(), new HashMap<>());
        adventure.setId((anAdventureData.getId()));
        adventure.setTitle(anAdventureData.getTitle());
        LocationMapper locationMapper = mapperSupporter.getMapper(LocationMapper.class);
        Set<LocationData> locationDataList = new HashSet<>(anAdventureData.getLocationData().values());
        List<Location> locationList = locationMapper.mapToBOs(List.copyOf(locationDataList));
        adventure.setLocations(locationList);
        adventure.setCurrentLocationId(anAdventureData.getCurrentLocationId());
        ItemContainerMapper containerMapper = mapperSupporter.getMapper(ItemContainerMapper.class);
        adventure.setPocket(containerMapper.mapToBO(anAdventureData.getPlayerPocket()));
        return adventure;
    }

    public AdventureData mapToDO(Adventure anAdventure) {
        final Vocabulary vocabulary = anAdventure.getVocabulary();
        final VocabularyMapper vocabularyMapper = mapperSupporter.getMapper(VocabularyMapper.class);
        final VocabularyData vocabularyData = vocabularyMapper.mapToDO(vocabulary);
        AdventureData adventureData = new AdventureData();
        adventureData.setVocabularyData(vocabularyData);
        adventureData.setId(anAdventure.getId());
        adventureData.setTitle(anAdventure.getTitle());
        adventureData.setCurrentLocationId(anAdventure.getCurrentLocationId());
        LocationMapper locationMapper = mapperSupporter.getMapper(LocationMapper.class);
        List<LocationData> locationDataList = locationMapper.mapToDOs(anAdventure.getLocations());
        Map<String, LocationData> adventureDataLocations = new HashMap<>();
        locationDataList.forEach(locationData -> adventureDataLocations.put(locationData.getId(), locationData));
        adventureData.setLocationData(adventureDataLocations);
        ItemContainerMapper containerMapper = mapperSupporter.getMapper(ItemContainerMapper.class);
        adventureData.setPlayerPocket(containerMapper.mapToDO(anAdventure.getPocket()));
        return adventureData;
    }
}
