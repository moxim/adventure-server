package com.pdg.adventure.server.mapper;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
@ComponentScan(basePackages = "com.pdg.adventure.server.support")
@AutoRegisterMapper(priority = 100, description = "Top-level adventure mapping with all dependencies")
public class AdventureMapper implements Mapper<AdventureData, Adventure> {

    private final MapperSupporter mapperSupporter;
    private final Mapper<VocabularyData, Vocabulary> vocabularyMapper;
    private final Mapper<LocationData, Location> locationMapper;
    private final Mapper<ItemContainerData, GenericContainer> containerMapper;
    private final AdventureConfig adventureConfig;

    public AdventureMapper(MapperSupporter aMapperSupporter,
                           VocabularyMapper aVocabularyMapper,
                           LocationMapper aLocationMapper,
                           ItemContainerMapper aContainerMapper,
                           AdventureConfig anAdventureConfig) {
        mapperSupporter = aMapperSupporter;
        vocabularyMapper = aVocabularyMapper;
        locationMapper = aLocationMapper;
        containerMapper = aContainerMapper;
        adventureConfig = anAdventureConfig;
        mapperSupporter.registerMapper(AdventureData.class, Adventure.class, this);
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
//        updateAdventureConfig(adventure);
        return adventure;
    }

    private void updateAdventureConfig(final Adventure adventure) {
        // Clear existing data to prevent stale references
        adventureConfig.allLocations().clear();
        adventureConfig.allItems().clear();
        adventureConfig.allContainers().clear();

        // Populate locations
        for (Location location : adventure.getLocations()) {
            adventureConfig.allLocations().put(location.getId(), location);
        }

        // Collect all items recursively from all containers
        for (Location location : adventure.getLocations()) {
            collectItemsRecursively(location.getItemContainer(), adventureConfig.allItems());
        }
        // Also collect from player pocket
        collectItemsRecursively(adventure.getPocket(), adventureConfig.allItems());

        // Collect all containers recursively
        // Add player pocket
        adventureConfig.allContainers().put(adventure.getPocket().getId(), adventure.getPocket());

        // Add location containers and nested containers
        for (Location location : adventure.getLocations()) {
            GenericContainer itemContainer = location.getItemContainer();
            adventureConfig.allContainers().put(itemContainer.getId(), itemContainer);
            collectContainersRecursively(itemContainer, adventureConfig.allContainers());
        }
    }

    /**
     * Recursively collects all items from a container and its nested containers.
     */
    private void collectItemsRecursively(Container container, Map<String, Item> allItems) {
        if (container == null) {
            return;
        }

        // Get contents from the container
        List<Containable> contents;
        if (container instanceof GenericContainer genericContainer) {
            contents = genericContainer.getContents();
        } else {
            // If it's not a GenericContainer, we can't access contents
            return;
        }

        if (contents == null) {
            return;
        }

        // Process each item in the container
        for (Containable containable : contents) {
            if (containable instanceof Item item) {
                allItems.put(item.getId(), item);

                // If the item is also a container, recurse into it
                if (item instanceof Container nestedContainer) {
                    collectItemsRecursively(nestedContainer, allItems);
                }
            }
        }
    }

    /**
     * Recursively collects all containers from a container and its nested items.
     */
    private void collectContainersRecursively(Container container, Map<String, Container> allContainers) {
        if (container == null) {
            return;
        }

        // Get contents from the container
        List<Containable> contents;
        if (container instanceof GenericContainer genericContainer) {
            contents = genericContainer.getContents();
        } else {
            return;
        }

        if (contents == null) {
            return;
        }

        // Process each item - if it's a container, add it and recurse
        for (Containable containable : contents) {
            if (containable instanceof Container nestedContainer) {
                allContainers.put(nestedContainer.getId(), nestedContainer);
                collectContainersRecursively(nestedContainer, allContainers);
            }
        }
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
