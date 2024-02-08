package com.pdg.adventure.server;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AdventureBuilderTest {
    @Autowired
    AdventureMapper adventureMapper;

//    @Test
    public void buildAdventure() {
        String adventureId = "Adventure1";
        AdventureData adventureData = new AdventureData();
        adventureData.setId(adventureId);

        List<LocationData> locationData = createLocations();

//        adventureData.setLocationData(Set.copyOf(locationData));
        adventureData.setCurrentLocationId(locationData.get(0).getId());
        ItemContainerData pocket = createContainerData("Pocket");
        adventureData.setPlayerPocket(pocket);
//        adventureData.getPlayerPocket().setDescriptionData(createDescriptionData("Pocket"));

        Adventure adventure = adventureMapper.mapToBO(adventureData);

        assertThat(adventure.getId()).isEqualTo(adventureId);
        List<Location> locations = adventure.getLocations();
        assertThat(locations).hasSize(3);
        boolean found = false;
        for (Location location : locations) {
            if (location.getId().equals(adventure.getCurrentLocationId())) {
                found = true;
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

    private static List<LocationData> createLocations() {
        List<LocationData> locationDataList = new ArrayList<>();

        locationDataList.add(createLocationData("Location1"));
        locationDataList.add(createLocationData("Location2"));
        locationDataList.add(createLocationData("Location3"));

        return locationDataList;
    }

    private static LocationData createLocationData(String aQualifier) {
        LocationData locationData = new LocationData();
        locationData.setId(aQualifier);
        locationData.setHasBeenVisited(false);
        locationData.setDescriptionData(createDescriptionData(aQualifier));
        locationData.setItemContainerData(createContainerData(aQualifier));
        return locationData;
    }

    public static ItemContainerData createContainerData(String aQualifier) {
        ItemContainerData itemContainerData = new ItemContainerData();
        itemContainerData.setId(aQualifier);
        itemContainerData.setMaxSize(99);
        itemContainerData.setDescriptionData(createDescriptionData(aQualifier + "_Container"));
//        itemContainerData.setContents(createItemDatas(aQualifier, itemContainerData));
        return itemContainerData;
    }

    private static List<ItemData> createItemDatas(String aQualifier, ItemContainerData aParentContainer) {
        List<ItemData> itemDataList = new ArrayList<>();
        itemDataList.add(createItemData(aQualifier + "_Item1", aParentContainer));
        itemDataList.add(createItemData(aQualifier + "_Item2", aParentContainer));
        itemDataList.add(createItemData(aQualifier + "_Item3", aParentContainer));
        return itemDataList;
    }

    private static ItemData createItemData(String aQualifier, ItemContainerData aParentContainer) {
        ItemData itemData = new ItemData();
        itemData.setId(aQualifier + 1);
        itemData.setDescriptionData(createDescriptionData(aQualifier));
        itemData.setWearable(true);
        itemData.setWorn(false);
        itemData.setContainable(true);
//        itemData.setParentContainer(aParentContainer);
        return itemData;
    }

    public static DescriptionData createDescriptionData(String aQualifier) {
        final DescriptionData descriptionData = new DescriptionData();
        descriptionData.setId(aQualifier);
//        descriptionData.setAdjective("adjective_" + aQualifier);
//        descriptionData.setNoun("noun_" + aQualifier);
        descriptionData.setShortDescription("short_desc_" + aQualifier);
        descriptionData.setLongDescription("long_desc_" + aQualifier);
        return descriptionData;
    }
}
