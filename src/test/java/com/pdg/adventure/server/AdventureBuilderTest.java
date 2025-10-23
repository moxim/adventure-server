package com.pdg.adventure.server;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.testhelper.TestSupporter;

@SpringBootTest//(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = com.pdg.adventure.server.AdventureConfig.class)
@ComponentScan(basePackages = "com.pdg.adventure.server.mapper")
@Import({com.pdg.adventure.server.AdventureConfig.class, com.pdg.adventure.server.mapper.AdventureMapper.class})
public class AdventureBuilderTest {
    @Autowired
    AdventureMapper adventureMapper;

    @Autowired
    MapperSupporter mapperSupporter;

    VocabularyData vocabularyData = new VocabularyData();

    @Test
    @Disabled
    void buildAdventure() {
        String adventureId = "Adventure1";
        final AdventureData adventureData = createAdventureData(adventureId);
        Adventure adventure = adventureMapper.mapToBO(adventureData);

        mapperSupporter.getVocabulary().createNewWord("noun_Location3", Word.Type.NOUN);

        assertThat(adventure.getId()).isEqualTo(adventureId);
        List<Location> locations = adventure.getLocations();
        assertThat(locations).hasSize(3);
        boolean found = false;
        for (Location location : locations) {
            if (location.getId().equals(adventure.getCurrentLocationId())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
        assertThat(adventure.getPocket().getId()).isEqualTo(adventureData.getPlayerPocket().getId());

        AdventureData andBack = adventureMapper.mapToDO(adventure);
        assertThat(andBack.getId()).isEqualTo(adventureId);
        assertThat(andBack.getLocationData()).size().isEqualTo(3);
        assertThat(andBack.getCurrentLocationId()).isEqualTo(adventure.getCurrentLocationId());
        assertThat(andBack.getPlayerPocket().getId()).isEqualTo(adventure.getPocket().getId());

        adventure.run();
    }

    private AdventureData createAdventureData(String adventureId) {
        AdventureData adventureData = new AdventureData(vocabularyData);
        adventureData.setId(adventureId);

        List<LocationData> locationData = createLocations();
        Map<String, LocationData> locationDataMap = new HashMap<>();
        locationData.forEach(location -> {
//            location.setAdventure(adventureData);
            locationDataMap.put(location.getId(), location);
        });
        adventureData.setLocationData(locationDataMap);

        adventureData.setCurrentLocationId(locationData.getFirst().getId());
        ItemContainerData pocket = createContainerData("Pocket");
        adventureData.setPlayerPocket(pocket);
        return adventureData;
    }

    private List<LocationData> createLocations() {
        List<LocationData> locationDataList = new ArrayList<>();

        locationDataList.add(createLocationData("Location1"));
        locationDataList.add(createLocationData("Location2"));
        locationDataList.add(createLocationData("Location3"));

        return locationDataList;
    }

    private LocationData createLocationData(String aQualifier) {
        LocationData locationData = createDummyLocationData(aQualifier);
        Set<DirectionData> directions = new HashSet<>();
        directions.add(createDirectionData(aQualifier, createDummyLocationData(aQualifier)));
        directions.add(createDirectionData(aQualifier, createDummyLocationData(aQualifier)));
        locationData.setDirectionsData(directions);
        return locationData;
    }

    private LocationData createDummyLocationData(String aQualifier) {
        LocationData locationData = new LocationData();
        locationData.setId(aQualifier);
        locationData.setHasBeenVisited(false);
        locationData.setDescriptionData(createDescriptionData(aQualifier));
        locationData.setItemContainerData(createContainerData(aQualifier));
        return locationData;
    }

    private DirectionData createDirectionData(String aDirection1, LocationData aLocationData) {
        DirectionData direction = new DirectionData();
        direction.setId(aDirection1);
        direction.setCommandData(createCommandData(aDirection1 + "_Command"));
        direction.setDestinationId(aLocationData.getId());
        direction.setDescriptionData(createDescriptionData(aDirection1 + "_Description"));
        return direction;
    }

    private CommandData createCommandData(String aCommand) {
        CommandData commandData = new CommandData();
        commandData.setId(aCommand);
        commandData.setCommandDescription(TestSupporter.createCommandDescriptionData(aCommand, vocabularyData));
        return commandData;
    }

    private ItemContainerData createContainerData(String aQualifier) {
        ItemContainerData itemContainerData = new ItemContainerData();
        itemContainerData.setId(aQualifier);
        itemContainerData.setMaxSize(99);
        itemContainerData.setDescriptionData(createDescriptionData(aQualifier + "_Container"));
//        itemContainerData.setContents(createItemDatas(aQualifier, itemContainerData));
        return itemContainerData;
    }

    private List<ItemData> createItemDatas(String aQualifier, ItemContainerData aParentContainer) {
        List<ItemData> itemDataList = new ArrayList<>();
        itemDataList.add(createItemData(aQualifier + "_Item1", aParentContainer));
        itemDataList.add(createItemData(aQualifier + "_Item2", aParentContainer));
        itemDataList.add(createItemData(aQualifier + "_Item3", aParentContainer));
        return itemDataList;
    }

    private ItemData createItemData(String aQualifier, ItemContainerData aParentContainer) {
        ItemData itemData = new ItemData();
        itemData.setId(aQualifier + 1);
        itemData.setDescriptionData(createDescriptionData(aQualifier));
        itemData.setWearable(true);
        itemData.setWorn(false);
        itemData.setContainable(true);
//        itemData.setParentContainer(aParentContainer);
        return itemData;
    }

    public DescriptionData createDescriptionData(String aQualifier) {
        final DescriptionData descriptionData = new DescriptionData();
        descriptionData.setId(aQualifier);

        Word adjective = new Word("adjective_" + aQualifier, Word.Type.ADJECTIVE);
        descriptionData.setAdjective(adjective);

        Word noun = new Word("noun_" + aQualifier, Word.Type.NOUN);
        descriptionData.setNoun(noun);

        descriptionData.setShortDescription("short_desc_" + aQualifier);
        descriptionData.setLongDescription("long_desc_" + aQualifier);

        vocabularyData.addWord(adjective);
        vocabularyData.addWord(noun);

        return descriptionData;
    }
}
