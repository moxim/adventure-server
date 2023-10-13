package com.pdg.adventure.server.storage;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.support.MapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdventureService {
    private final AdventureRepository adventureRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public AdventureService(LocationRepository aLocationRepository,
                            AdventureRepository anAdventureRepository, MapperProvider aMappingProvider) {
        locationRepository = aLocationRepository;
        adventureRepository = anAdventureRepository;
    }

    public LocationData findLocationById(@Nonnull String id) {
        final Optional<LocationData> byId = locationRepository.findById(id);
        LocationData result;
        if (byId.isPresent()) {
            result = byId.get();
        } else {
           result = new LocationData();
           result.setId(UUID.randomUUID().toString());
        }
        return result;
    }

    public void saveLocationData(LocationData aLocationData) {
        locationRepository.save(aLocationData);
    }

    public List<LocationData> getLocations() {
        return locationRepository.findAll();
    }

    public int getCountOfLocations() {
        return getLocations().size();
    }

    public void saveAdventureData(AdventureData anAdventure) {
        adventureRepository.save(anAdventure);
    }

    public AdventureData findAdventureById(String anId) {
        final Optional<AdventureData> byId = adventureRepository.findById(anId);
        AdventureData result;
        if (byId.isPresent()) {
            result = byId.get();
        } else {
           result = new AdventureData();
           result.setId(UUID.randomUUID().toString());
        }
        postProcess(result);
        return result;
    }

    private void postProcess(AdventureData anAdventureData) {
//        final Vocabulary vocabulary = anAdventureData.getVocabulary();
//        final Collection<Word> allWords = vocabulary.getWords();
//        for (Word word : allWords) {
//            word.setSynonym(word.getSynonym());
//        }
//        for (Map.Entry<String, Word> entry : allWords.entrySet()) {
//            vocabulary.addSynonymForWord(entry.getKey(), allWords.get(entry.getKey()));
//        }
    }

    public List<AdventureData> getAdventures() {
        return adventureRepository.findAll();
    }

    public void deleteLocation(String anId) {
        locationRepository.deleteById(anId);
    }

    public void deleteAdventure(String anId) {
        adventureRepository.deleteById(anId);
    }
}
