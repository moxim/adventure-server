package com.pdg.adventure;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.AdventureService;

@SpringBootTest
class MiniAdventureTest {

    @Autowired
    AdventureService adventureService;

    @Autowired
    AdventureMapper adventureMapper;

    @Test
    @Disabled("Disabled until DescriptionData is available!")
    void testGameRun() {
        final List<AdventureData> adventures = adventureService.getAdventures();
        final AdventureData adventureData = adventures.getFirst();
        Adventure adventure = adventureMapper.mapToBO(adventureData);

        MiniAdventure miniAdventure = new MiniAdventure(new AdventureConfig());
        miniAdventure.setLocations(adventure.getLocations());
        miniAdventure.run();
    }
}
