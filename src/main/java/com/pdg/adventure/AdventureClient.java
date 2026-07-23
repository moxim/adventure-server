package com.pdg.adventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadAdventureAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.mapper.WorkflowMapper;
import com.pdg.adventure.server.storage.service.AdventureService;

/**
 * The entry point of a Spring Boot application.
 */
@SpringBootApplication
public class AdventureClient implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdventureClient.class);

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final WorkflowMapper workflowMapper;
    private final AdventureConfig adventureConfig;
    private final Environment environment;

    public AdventureClient(@Lazy AdventureService anAdventureService, @Lazy AdventureMapper anAdventureMapper,
                           @Lazy WorkflowMapper aWorkflowMapper, @Lazy AdventureConfig anAdventureConfig,
                           Environment anEnvironment) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        workflowMapper = aWorkflowMapper;
        adventureConfig = anAdventureConfig;
        environment = anEnvironment;
    }

    @Override
    public void run(String... args) throws Exception {
        // This CommandLineRunner boots the demo/manual game loop. Test slices (@DataMongoTest etc.)
        // still start the real SpringApplication, which runs every CommandLineRunner bean - so without
        // this guard, unrelated data-layer tests would try to play the game during context startup.
        if (environment.matchesProfiles("test")) {
            return;
        }

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
                                                                                gameContext);
        loadAdventureAction.setAdventureId(adventureId);
        try {
            loadAdventureAction.loadAdventure(adventureId);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warn("Ignoring failed adventure load with ID {}: {}", adventureId, e.getMessage());
            // ignore it this time
        }

        MiniAdventure miniAdventure = new MiniAdventure(adventureConfig, adventureMapper, workflowMapper,
                                                         adventureService, gameContext,
                                                         adventureData.getVocabularyData());
        Thread.sleep(3000); // Wait for 3 seconds to let the user read the messages

        miniAdventure.run();
    }

    static void main(String[] args) {
        SpringApplication.run(AdventureClient.class, args);
    }
}
