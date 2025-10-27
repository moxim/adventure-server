package com.pdg.adventure;

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
import com.pdg.adventure.server.storage.MessageService;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * The entry point of a Spring Boot application.
 */
//@SpringBootApplication
public class AdventureClient implements CommandLineRunner {

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final MessageService messageService;
    private final AdventureConfig adventureConfig;
    private final VocabularyMapper vocabularyMapper;

    public AdventureClient(AdventureService adventureService, AdventureMapper adventureMapper,
                          MessageService messageService, AdventureConfig adventureConfig,
                          VocabularyMapper vocabularyMapper) {
        this.adventureService = adventureService;
        this.adventureMapper = adventureMapper;
        this.messageService = messageService;
        this.adventureConfig = adventureConfig;
        this.vocabularyMapper = vocabularyMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        final List<AdventureData> adventures = adventureService.getAdventures();
        final AdventureData adventureData = adventures.getFirst();

        // Load vocabulary from MongoDB and populate BEFORE mapping so synonyms are available
        Vocabulary vocabulary = vocabularyMapper.mapToBO(adventureData.getVocabularyData());
        // Vocabulary is already populated in adventureConfig by the mapper

        // Load messages from MongoDB and populate MessagesHolder BEFORE mapping
        List<MessageData> messages = messageService.getAllMessagesForAdventure(adventureData.getId());
        for (MessageData messageData : messages) {
            adventureConfig.allMessages().addMessage(messageData.getMessageId(), messageData.getText());
        }

        // Now map the adventure - MessageActionMapper will find messages, CommandMappers will find words/synonyms
        Adventure savedAdventure = adventureMapper.mapToBO(adventureData);

        savedAdventure.getLocations()
                      .forEach(location -> adventureConfig.allLocations().put(location.getId(), location));

        MiniAdventure miniAdventure = new MiniAdventure(adventureConfig);

        final String starrtLocationId = savedAdventure.getCurrentLocationId();
        Location startLocation = adventureConfig.allLocations().get(starrtLocationId);
        Environment.setCurrentLocation(startLocation);

        Environment.setUpWorkflows();
        Environment.setPocket(new GenericContainer(new DescriptionProvider("your pocket"), 5));

        miniAdventure.run();
    }

    public static void main(String[] args) {
        //        LaunchUtil.launchBrowserInDevelopmentMode(
        SpringApplication.run(AdventureClient.class, args)
        //        )
        ;
    }
}
