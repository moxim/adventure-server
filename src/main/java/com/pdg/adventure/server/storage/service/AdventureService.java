package com.pdg.adventure.server.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.repository.AdventureRepository;
import com.pdg.adventure.server.storage.repository.LocationRepository;
import com.pdg.adventure.server.storage.repository.PictureRepository;
import com.pdg.adventure.server.storage.repository.VocabularyRepository;
import com.pdg.adventure.server.storage.repository.WordRepository;

@Service
public class AdventureService {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureService.class);

    private final AdventureRepository adventureRepository;
    private final LocationRepository locationRepository;
    private final WordRepository wordRepository;
    private final VocabularyRepository vocabularyRepository;
    private final CascadeDeleteHelper cascadeDeleteHelper;
    private final PictureRepository pictureRepository;
    private final BuilderVersion builderVersion;

    public AdventureService(LocationRepository aLocationRepository,
                            AdventureRepository anAdventureRepository,
                            WordRepository aWordRepository,
                            VocabularyRepository aVocabularyRepository,
                            CascadeDeleteHelper aCascadeDeleteHelper,
                            PictureRepository aPictureRepository,
                            BuilderVersion aBuilderVersion) {
        locationRepository = aLocationRepository;
        adventureRepository = anAdventureRepository;
        wordRepository = aWordRepository;
        vocabularyRepository = aVocabularyRepository;
        cascadeDeleteHelper = aCascadeDeleteHelper;
        pictureRepository = aPictureRepository;
        builderVersion = aBuilderVersion;
    }

    public LocationData findLocationById(String id) {
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

    public void savePictureData(PictureData aPictureData) {
        LOG.debug("Saving picture data: {}", aPictureData);
        LOG.info("Saving picture data: {}", aPictureData.getId());
        pictureRepository.save(aPictureData);
    }

    public void deletePicture(String anId) {
        LOG.info("Deleting picture: {}", anId);
        pictureRepository.findById(anId).ifPresentOrElse(
                pictureRepository::delete,
                () -> LOG.warn("Picture not found for deletion: {}", anId));
    }

    public void saveAdventureData(AdventureData anAdventure) {
        LOG.debug("Saving adventure data: {}", anAdventure);
        LOG.info("Saving adventure data: {}", anAdventure.getId());
        preProcess(anAdventure);
        builderVersion.current().ifPresent(anAdventure::setBuilderVersion);
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

    public void deleteWord(Word aWord) {
        LOG.debug("Deleting word data: {}", aWord);
        LOG.info("Deleting word data: {}", aWord.getId());
        wordRepository.delete(aWord);
    }

    private void preProcess(AdventureData anAdventure) {
//        final Set<Word> words = anAdventure.getWords();
//        words.clear();
//        final Vocabulary vocabulary = anAdventure.getVocabulary();
//        words.addAll(vocabulary.getWords());
    }

    public Optional<AdventureData> findAdventureById(String anId) {
        return adventureRepository.findById(anId);
    }

    public List<AdventureData> getAdventures() {
        return adventureRepository.findAll();
    }

    public List<AdventureData> getAdventuresByIds(Collection<String> ids) {
        return adventureRepository.findAllById(ids);
    }

    public void deleteLocation(String anId) {
        LOG.info("Deleting location: {}", anId);

        Optional<LocationData> locationOpt = locationRepository.findById(anId);

        if (locationOpt.isPresent()) {
            LocationData location = locationOpt.get();

            // A location owns its item container (and the items in it); without the cascade
            // those documents become unreachable orphans once the location is gone.
            cascadeDeleteHelper.cascadeDelete(location);
            locationRepository.delete(location);
        } else {
            LOG.warn("Location not found for deletion: {}", anId);
        }
    }

    public void deleteAdventure(String anId) {
        LOG.debug("Deleting adventure: {}", anId);

        // Load the adventure first to ensure cascade delete can access all relationships
        Optional<AdventureData> adventureOpt = adventureRepository.findById(anId);

        if (adventureOpt.isPresent()) {
            AdventureData adventure = adventureOpt.get();

            // Perform cascade delete on all @CascadeDelete annotated fields
            // (messages, locations, items, vocabulary, words)
            LOG.debug("Performing cascade delete for adventure: {}", anId);
            cascadeDeleteHelper.cascadeDelete(adventure);

            // Now delete the adventure itself
            LOG.debug("Deleting adventure document: {}", anId);
            adventureRepository.delete(adventure);
            LOG.info("Adventure deleted successfully: {}", anId);
        } else {
            LOG.warn("Adventure not found for deletion: {}", anId);
        }
    }

}
