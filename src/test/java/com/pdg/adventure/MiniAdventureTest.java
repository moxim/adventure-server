package com.pdg.adventure;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;

@SpringBootTest
class MiniAdventureTest {

    @Autowired
    AdventureService adventureService;

    @Autowired
    AdventureMapper adventureMapper;

    @Test
//    @Disabled("Disabled until DescriptionData is available!")
    void testGameRun() {
        final List<AdventureData> adventures = adventureService.getAdventures();
        final AdventureData adventureData = adventures.getFirst();
        Adventure adventure = adventureMapper.mapToBO(adventureData);

        MiniAdventure miniAdventure = new MiniAdventure(adventure.getVocabulary(), new HashMap<>(4), new MessagesHolder(),
                                                             new GenericContainer(new DescriptionProvider("all items"), 9999));
        miniAdventure.setLocations(adventure.getLocations());
        miniAdventure.run();
    }

}
