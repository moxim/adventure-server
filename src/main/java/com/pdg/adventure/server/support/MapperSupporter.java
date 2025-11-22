package com.pdg.adventure.server.support;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
public class MapperSupporter {
    Logger logger = LoggerFactory.getLogger(MapperSupporter.class);

    @Getter
    private final Vocabulary vocabulary;
    @Getter
    private final VariableProvider variableProvider;
    @Getter
    private final MessagesHolder messagesHolder;
    @Getter
    private final Map<String, Item> mappedItems;
    @Getter
    private final Map<String, Location> mappedLocations;
    @Getter
    private final Map<String, Container> mappedContainers;

    private final Map<Class<?>, Mapper<?, ?>> mapperMap;
    private final AdventureConfig adventureConfig;


    public MapperSupporter(AdventureConfig anAdventureConfig) {
        adventureConfig = anAdventureConfig;
        variableProvider = adventureConfig.allVariables();
        messagesHolder = adventureConfig.allMessages();
        mappedItems = adventureConfig.allItems();
        vocabulary = adventureConfig.allWords();
        mappedLocations = adventureConfig.allLocations();
        mappedContainers = adventureConfig.allContainers();
        mapperMap = new HashMap<>();
    }

    public void addMappedLocation(Location aLocation) {
        mappedLocations.put(aLocation.getId(), aLocation);
    }

    public Location getMappedLocation(String aLocationId) {
        return mappedLocations.get(aLocationId);
    }

    /**
     * This method returns a mapper for the given DTO class.
     *
     * @param aDataObjectMapperName The class of the data object.
     * @param <BO>                  The type of the business object.
     * @param <DO>                  The type of the data object.
     * @return The mapper for the given DTO class.
     */
    public <BO, DO> Mapper<BO, DO> getMapper(Class<?> aDataObjectMapperName) {
        return (Mapper<BO, DO>) mapperMap.get(aDataObjectMapperName);
    }

    /**
     * This method registers a mapper with the provider.
     *
     * @param aDOClass The class of the data object.
     * @param aBOClass The class of the business object.
     * @param aMapper  The mapper to register.
     * @param <BO>     The type of the business object.
     * @param <DO>     The type of the data object.
     */
    public <BO, DO> void registerMapper(Class<?> aDOClass, Class<?> aBOClass, Mapper<DO, BO> aMapper) {
        mapperMap.put(aBOClass, aMapper);
        mapperMap.put(aDOClass, aMapper);
    }

    @PostConstruct
    public void completeInit() {
        logger.info("MapperSupporter is initialized");
    }

    public String toString() {
        return "[MapperSupporter] " +
               "vocabulary: " + vocabulary + ", " +
               "variableProvider: " + variableProvider + ", " +
               "messagesHolder: " + messagesHolder + ", " +
               "allItems: " + mappedItems + ", " +
               "mappedLocations: " + mappedLocations + ", " +
               "mapperMap: " + mapperMap;
    }

    private AdventureConfig getAdventrueConfig() {
        return adventureConfig;
    }

    public void addMappedItem(final Item anItem) {
        mappedItems.put(anItem.getId(), anItem);
    }

    public Item getMappedItem(final String anId) {
        return mappedItems.get(anId);
    }

    public Container getMappedContainer(final String aParentContainerId) {
        return mappedContainers.get(aParentContainerId);
    }

    public void addMappedContainer(final GenericContainer aContainer) {
        mappedContainers.put(aContainer.getId(), aContainer);
    }
}
