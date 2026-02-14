package com.pdg.adventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Lazy;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadAdventureAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.AdventureService;

/**
 * The entry point of a Spring Boot application.
 */
//@SpringBootApplication
public class AdventureClient implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdventureClient.class);

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final AdventureConfig adventureConfig;

    public AdventureClient(AdventureService anAdventureService, AdventureMapper anAdventureMapper,
                           @Lazy AdventureConfig anAdventureConfig) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        adventureConfig = anAdventureConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        final List<AdventureData> adventures = adventureService.getAdventures();
        if (adventures.isEmpty()) {
            LOG.error("No adventures found in the database. Please add an adventure and try again.");
            return;
        }

        GameContext gameContext = adventureConfig.gameContext();
        final AdventureData adventureData = adventures.getFirst();
        String adventureId = adventureData.getId();

        final LoadAdventureAction loadAdventureAction = new LoadAdventureAction(adventureService, adventureMapper,
                                                                                adventureConfig,
                                                                                adventureConfig.allMessages(),
                                                                                gameContext);
        loadAdventureAction.setAdventureId(adventureId);
        try {
            loadAdventureAction.loadAdventure(adventureId);
        } catch (Exception e) {
            LOG.warn("Ignoring failed adventure load with ID {}: {}", adventureId, e.getMessage());
            // ignore it this time
        }

        MiniAdventure miniAdventure = new MiniAdventure(adventureConfig, adventureMapper, adventureService,
                                                         gameContext);
        Thread.sleep(3000); // Wait for 3 seconds to let the user read the messages

        miniAdventure.run();
    }

    static void main(String[] args) {
        //        LaunchUtil.launchBrowserInDevelopmentMode(
        SpringApplication.run(AdventureClient.class, args)
        //        )
        ;
    }
}
