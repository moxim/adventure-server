package com.pdg.adventure.server.mapper;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
@ComponentScan(basePackages = "com.pdg.adventure.server.support")
@AutoRegisterMapper(priority = 100, description = "Top-level adventure mapping with all dependencies")
public class AdventureMapper implements Mapper<AdventureData, Adventure> {

    private final MapperSupporter mapperSupporter;
    private final Mapper<VocabularyData, Vocabulary> vocabularyMapper;
    private final LocationMapper locationMapper;
    private final ItemContainerMapper containerMapper;

    public AdventureMapper(MapperSupporter aMapperSupporter,
                           VocabularyMapper aVocabularyMapper,
                           LocationMapper aLocationMapper,
                           ItemContainerMapper aContainerMapper) {
        mapperSupporter = aMapperSupporter;
        vocabularyMapper = aVocabularyMapper;
        locationMapper = aLocationMapper;
        containerMapper = aContainerMapper;
    }

    public Adventure mapToBO(AdventureData anAdventureData) {
        final Vocabulary vocabulary = vocabularyMapper.mapToBO(anAdventureData.getVocabularyData());
        Adventure adventure = new Adventure(vocabulary, new HashMap<>(4), new MessagesHolder(), new HashMap<>());
        adventure.setId((anAdventureData.getId()));
        adventure.setTitle(anAdventureData.getTitle());
        Set<LocationData> locationDataSet = new HashSet<>(anAdventureData.getLocationData().values());
        List<LocationData> locationDataList = List.copyOf(locationDataSet);
        // Commands (and their conditions/actions) resolve item references by id, and may point at
        // an item anywhere in the adventure. So: register every location, container and item first
        // — including the player's pocket — and only then map any commands.
        List<Location> locationList = locationMapper.createLocations(locationDataList);
        locationMapper.registerItems(locationDataList);
        GenericContainer pocket = containerMapper.registerContainer(anAdventureData.getPlayerPocket());
        locationMapper.mapCommands(locationDataList);
        containerMapper.mapItemCommands(anAdventureData.getPlayerPocket());
        adventure.setLocations(locationList);
        adventure.setCurrentLocationId(anAdventureData.getCurrentLocationId());
        adventure.setPocket(pocket);
        return adventure;
    }

    public AdventureData mapToDO(Adventure anAdventure) {
        final Vocabulary vocabulary = anAdventure.getVocabulary();
        final VocabularyData vocabularyData = vocabularyMapper.mapToDO(vocabulary);
        AdventureData adventureData = new AdventureData();
        adventureData.setVocabularyData(vocabularyData);
        adventureData.setId(anAdventure.getId());
        adventureData.setTitle(anAdventure.getTitle());
        adventureData.setCurrentLocationId(anAdventure.getCurrentLocationId());
        List<LocationData> locationDataList = locationMapper.mapToDOs(anAdventure.getLocations());
        Map<String, LocationData> adventureDataLocations = new HashMap<>();
        locationDataList.forEach(locationData -> adventureDataLocations.put(locationData.getId(), locationData));
        adventureData.setLocationData(adventureDataLocations);
        adventureData.setPlayerPocket(containerMapper.mapToDO(anAdventure.getPocket()));
        return adventureData;
    }
}
