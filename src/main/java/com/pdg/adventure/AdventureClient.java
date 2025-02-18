package com.pdg.adventure;

import org.springframework.boot.SpringApplication;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;

/**
 * The entry point of a Spring Boot application.
 */
//@SpringBootApplication
public class AdventureClient {

    AdventureService adventureService;

    AdventureMapper adventureMapper;

    public static void main(String[] args) {
        //        LaunchUtil.launchBrowserInDevelopmentMode(
        SpringApplication.run(AdventureClient.class, args)
        //        )
        ;
    }

    public AdventureClient(AdventureService adventureService, AdventureMapper adventureMapper) {
        final List<AdventureData> adventures = adventureService.getAdventures();
        final AdventureData adventureData = adventures.getFirst();

        Adventure savedAdventure = adventureMapper.mapToBO(adventureData);

        AdventureConfig adventureConfig = new AdventureConfig();
        savedAdventure.getLocations().forEach(
            location -> adventureConfig.allLocations().put(location.getId(), location)
        );
        adventureConfig.allWords().addWords(savedAdventure.getVocabulary().getWords());

        MiniAdventure miniAdventure = new MiniAdventure(adventureConfig);

        Environment.setCurrentLocation(savedAdventure.getLocations().getFirst());
        Environment.setUpWorkflows();
        Environment.setPocket(new GenericContainer(new DescriptionProvider("your pocket"), 5));
        miniAdventure.setUpMessages();
        miniAdventure.run();
    }
}
