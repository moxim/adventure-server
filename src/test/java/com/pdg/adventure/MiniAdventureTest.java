package com.pdg.adventure;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.AdventureService;

@SpringBootTest
class MiniAdventureTest {

    @Autowired
    AdventureService adventureService;

    @Autowired
    AdventureMapper adventureMapper;

    @Autowired
    GameContext gameContext;

    @Autowired
    AdventureConfig adventureConfig;

    @Test
    @Disabled("Disabled until the engine can be feed with commands automatically.")
    void testGameRun() {
        final List<AdventureData> adventures = adventureService.getAdventures();
        final AdventureData adventureData = adventures.getFirst();
        Adventure adventure = adventureMapper.mapToBO(adventureData);

        MiniAdventure miniAdventure = new MiniAdventure(adventureConfig, adventureMapper, adventureService,
                                                         gameContext);
        miniAdventure.setLocations(adventure.getLocations());
        miniAdventure.run();
    }
}
