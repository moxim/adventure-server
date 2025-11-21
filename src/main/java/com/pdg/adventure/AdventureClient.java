package com.pdg.adventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.mapper.VocabularyMapper;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * The entry point of a Spring Boot application.
 */
//@SpringBootApplication
public class AdventureClient implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdventureClient.class);

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final AdventureConfig adventureConfig;
    private final VocabularyMapper vocabularyMapper;

    public AdventureClient(AdventureService adventureService, AdventureMapper adventureMapper,
                           AdventureConfig adventureConfig,
                           VocabularyMapper vocabularyMapper) {
        this.adventureService = adventureService;
        this.adventureMapper = adventureMapper;
        this.adventureConfig = adventureConfig;
        this.vocabularyMapper = vocabularyMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        final List<AdventureData> adventures = adventureService.getAdventures();
        if (adventures.isEmpty()) {
            LOG.error("No adventures found in the database. Please add an adventure and try again.");
            return;
        }

        final AdventureData adventureData = adventures.getFirst();
        LOG.info("Loaded adventure: {}", adventureData.getTitle());

        // Load vocabulary from MongoDB and populate BEFORE mapping so synonyms are available
        Vocabulary vocabulary = vocabularyMapper.mapToBO(adventureData.getVocabularyData());
        // Vocabulary is already populated in adventureConfig by the mapper

        // Load messages from adventure's messages Map (loaded via @DBRef) and populate MessagesHolder BEFORE mapping
        for (MessageData messageData : adventureData.getMessages().values()) {
            adventureConfig.allMessages().addMessage(messageData.getMessageId(), messageData.getText());
        }

        Adventure savedAdventure = adventureMapper.mapToBO(adventureData);

        final var adventureLocations = savedAdventure.getLocations();

        if (adventureLocations.isEmpty()) {
            LOG.error("The adventure '{}' has no locations defined. Please add locations and try again.",
                      adventureData.getTitle());
            return;
        }

        adventureLocations.forEach(location -> adventureConfig.allLocations().put(location.getId(), location));

        MiniAdventure miniAdventure = new MiniAdventure(adventureConfig);
        miniAdventure.setUpMessages();

        String startLocationId = savedAdventure.getCurrentLocationId();
        Location startLocation = adventureConfig.allLocations().get(startLocationId);

        if (startLocation == null) {
            LOG.error("Warning: Could not find location with ID: '{}'", startLocationId);
            LOG.error("Available location IDs: {}", adventureConfig.allLocations().keySet());
            // Fallback to first available location
            startLocation = adventureLocations.getFirst();
            LOG.error("Using fallback location: {}", startLocation.getId());
        }

        Environment.setCurrentLocation(startLocation);

        Environment.setUpWorkflows();
        Environment.setPocket(savedAdventure.getPocket());

        miniAdventure.run();
    }

    static void main(String[] args) {
        //        LaunchUtil.launchBrowserInDevelopmentMode(
        SpringApplication.run(AdventureClient.class, args)
        //        )
        ;
    }
}
