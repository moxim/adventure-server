package com.pdg.adventure.server.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
public class AdventureService {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureService.class);

    private final AdventureRepository adventureRepository;
    private final LocationRepository locationRepository;
    private final WordRepository wordRepository;
    private final VocabularyReporitory vocabularyRepository;

//    @Autowired
    public AdventureService(LocationRepository aLocationRepository,
                            AdventureRepository anAdventureRepository,
                            WordRepository aWordRepository,
                            VocabularyReporitory aVocabularyRepository,
                            MapperSupporter aMappingService) {
        locationRepository = aLocationRepository;
        adventureRepository = anAdventureRepository;
        wordRepository = aWordRepository;
        vocabularyRepository = aVocabularyRepository;
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
        LOG.debug("Saving location data: {}", aLocationData);
        LOG.info("Saving location data: {}", aLocationData.getId());
        locationRepository.save(aLocationData);
    }

    public List<LocationData> getLocations() {
        return locationRepository.findAll();
    }

    public int getCountOfLocations() {
        return getLocations().size();
    }

    public void saveAdventureData(AdventureData anAdventure) {
        LOG.debug("Saving adventure data: {}", anAdventure);
        LOG.info("Saving adventure data: {}", anAdventure.getId());
        preProcess(anAdventure);
        adventureRepository.save(anAdventure);
    }

    public void saveVocabularyData(VocabularyData aVocabularyData) {
        LOG.debug("Saving vocabulary data: {}", aVocabularyData);
        LOG.info("Saving vocabulary data: {}", aVocabularyData.getId());
        vocabularyRepository.save(aVocabularyData);
    }

    public void saveWordData(Collection<Word> aNumberOfWords) {
        LOG.debug("Saving word data: {}", aNumberOfWords);
        LOG.info("Saving word data: {}", aNumberOfWords.size());
        wordRepository.saveAll(aNumberOfWords);
    }

    private void preProcess(AdventureData anAdventure) {
//        final Set<Word> words = anAdventure.getWords();
//        words.clear();
//        final Vocabulary vocabulary = anAdventure.getVocabulary();
//        words.addAll(vocabulary.getWords());
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
//        Vocabulary vocabulary = new Vocabulary();
//        anAdventureData.setVocabulary(vocabulary);
//        vocabulary.putWords(anAdventureData.getWords());
//
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

    public void deleteWord(String id) {wordRepository.deleteById(id);}

    public void deleteVocabulary(String id) {
        vocabularyRepository.deleteById(id);
    }
}
