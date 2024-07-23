package com.pdg.adventure.server.support;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapperSupporter {
    private final Vocabulary vocabulary;
    private final Map<String, Item> bagOfItems;
    private final MapperProvider mapperProvider;
    private final Map<String, Location> mappedLocations;

    public MapperSupporter(Vocabulary aVocabulary) {
        mapperProvider = new MapperProvider(this);
        mappedLocations = new HashMap<>();
        bagOfItems = new HashMap<>();
        vocabulary = new Vocabulary();
    }

    public void addMappedLocation(Location aLocation) {
        mappedLocations.put(aLocation.getId(), aLocation);
    }

    public Location getMappedLocation(String aLocationId) {
        return mappedLocations.get(aLocationId);
    }

    public MapperProvider getMapperProvider() {
        return mapperProvider;
    }

    public <T extends Mapper> T getMapper(Class<T> aMapperName) {
        return (T) mapperProvider.getMapper(aMapperName);
    }

    public <BO, DO> Mapper<BO, DO> registerMapper(Class<? extends Mapper<DO, BO>> aMapperClass,
                                                  Mapper<DO, BO> aMapper) {
        return mapperProvider.registerMapper(aMapperClass, aMapper);
    }

    public Map<String, Item> getBagOfItems() {
        return bagOfItems;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }
}
