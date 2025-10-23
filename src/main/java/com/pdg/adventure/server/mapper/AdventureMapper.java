package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
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
    private Mapper<VocabularyData, Vocabulary> vocabularyMapper;
    private Mapper<LocationData, Location> locationMapper;
    private Mapper<ItemContainerData, GenericContainer> containerMapper;

    public AdventureMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void initializeDependencies() {
        vocabularyMapper = mapperSupporter.getMapper(VocabularyData.class);
        locationMapper = mapperSupporter.getMapper(LocationData.class);
        containerMapper = mapperSupporter.getMapper(ItemContainerData.class);
    }

    public Adventure mapToBO(AdventureData anAdventureData) {
        final Vocabulary vocabulary = vocabularyMapper.mapToBO(anAdventureData.getVocabularyData());
        Adventure adventure = new Adventure(vocabulary, new HashMap<>(4), new MessagesHolder(), new HashMap<>());
        adventure.setId((anAdventureData.getId()));
        adventure.setTitle(anAdventureData.getTitle());
        Set<LocationData> locationDataList = new HashSet<>(anAdventureData.getLocationData().values());
        List<Location> locationList = locationMapper.mapToBOs(List.copyOf(locationDataList));
        adventure.setLocations(locationList);
        adventure.setCurrentLocationId(anAdventureData.getCurrentLocationId());
        adventure.setPocket(containerMapper.mapToBO(anAdventureData.getPlayerPocket()));
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
