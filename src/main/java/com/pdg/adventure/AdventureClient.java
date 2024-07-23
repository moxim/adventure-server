package com.pdg.adventure;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import org.springframework.boot.SpringApplication;

import java.util.HashMap;
import java.util.List;

/**
 * The entry point of a Spring Boot application.
 */
// @SpringBootApplication
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

        Adventure adventure = adventureMapper.mapToBO(adventureData);

        MiniAdventure miniAdventure = new MiniAdventure(adventure.getVocabulary(), new HashMap<>(4), new MessagesHolder(),
                new GenericContainer(new DescriptionProvider("all items"), 9999));
        miniAdventure.setLocations(adventure.getLocations());

        Environment.setCurrentLocation(adventure.getLocations().getFirst());
        Environment.setUpWorkflows();
        Environment.setPocket(new GenericContainer(new DescriptionProvider("your pocket"), 5));

        miniAdventure.run();
    }
}
