package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.Direction;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoMapperRegistrationProcessor;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.condition.CarriedConditionMapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Integration tests for reference resolution during adventure mapping.
 * <p>
 * Conditions and actions reference items by id and resolve them against
 * {@code AdventureConfig.allItems()} while the adventure is being mapped. These tests
 * pin down that such references resolve regardless of where the referenced item lives
 * (same location, another location, the player's pocket) — i.e. that all items are
 * registered before any command is mapped.
 */
@ExtendWith(SpringExtension.class)
@Import({AdventureConfig.class, MapperSupporter.class, AutoMapperRegistrationProcessor.class,
         AdventureMapper.class, VocabularyMapper.class, LocationMapper.class, ItemContainerMapper.class,
         ItemMapper.class, DirectionMapper.class, CommandMapper.class, CommandDescriptionMapper.class,
         CommandChainMapper.class, CommandProviderMapper.class, DescriptionMapper.class,
         CarriedConditionMapper.class})
class AdventureMapperReferenceResolutionTest {

    @MockitoBean
    GameContext gameContext;

    @Autowired
    private AdventureMapper adventureMapper;

    @Test
    @DisplayName("direction condition referencing an item in the same location resolves the item")
    void directionCondition_resolvesItemInSameLocation() {
        AdventureData adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setCurrentLocationId("hall");

        ItemData brassKey = createItem("brass-key", "brass", "key");

        LocationData hall = createLocation("hall");
        hall.getItemContainerData().getItems().add(brassKey);
        hall.getDirectionsData().add(createGuardedDirection("maze-door", "maze", "enter||maze", "brass-key"));

        adventureData.getLocationData().put("hall", hall);
        adventureData.getLocationData().put("maze", createLocation("maze"));

        Adventure adventure = adventureMapper.mapToBO(adventureData);

        CarriedCondition condition = firstCarriedCondition(adventure, "hall");
        assertThat(condition.getItem())
                .as("CarriedCondition must hold the mapped item, not null")
                .isNotNull();
        assertThat(condition.getItem().getId()).isEqualTo("brass-key");
    }

    @Test
    @DisplayName("direction condition referencing an item in the player's pocket resolves the item")
    void directionCondition_resolvesItemInPlayerPocket() {
        AdventureData adventureData = new AdventureData();
        adventureData.setId("adv-2");
        adventureData.setCurrentLocationId("cave");

        ItemData torch = createItem("torch", "burning", "torch");
        adventureData.getPlayerPocket().getItems().add(torch);

        LocationData cave = createLocation("cave");
        cave.getDirectionsData().add(createGuardedDirection("tunnel", "pit", "enter||tunnel", "torch"));

        adventureData.getLocationData().put("cave", cave);
        adventureData.getLocationData().put("pit", createLocation("pit"));

        Adventure adventure = adventureMapper.mapToBO(adventureData);

        CarriedCondition condition = firstCarriedCondition(adventure, "cave");
        assertThat(condition.getItem())
                .as("CarriedCondition must resolve an item carried in the player's pocket")
                .isNotNull();
        assertThat(condition.getItem().getId()).isEqualTo("torch");
    }

    @Test
    @DisplayName("condition referencing an unknown item id fails fast at mapping time")
    void condition_referencingUnknownItem_failsFast() {
        AdventureData adventureData = new AdventureData();
        adventureData.setId("adv-3");
        adventureData.setCurrentLocationId("yard");

        LocationData yard = createLocation("yard");
        yard.getDirectionsData().add(createGuardedDirection("gate", "field", "open||gate", "no-such-item"));

        adventureData.getLocationData().put("yard", yard);
        adventureData.getLocationData().put("field", createLocation("field"));

        assertThatThrownBy(() -> adventureMapper.mapToBO(adventureData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no-such-item");
    }

    private ItemData createItem(String anId, String anAdjective, String aNoun) {
        ItemData itemData = new ItemData();
        itemData.setId(anId);
        itemData.getDescriptionData().setAdjective(new Word(anAdjective, Word.Type.ADJECTIVE));
        itemData.getDescriptionData().setNoun(new Word(aNoun, Word.Type.NOUN));
        return itemData;
    }

    private LocationData createLocation(String anId) {
        LocationData locationData = new LocationData();
        locationData.setId(anId);
        locationData.getDescriptionData().setNoun(new Word(anId, Word.Type.NOUN));
        locationData.getDescriptionData().setShortDescription(anId);
        locationData.getItemContainerData().setId(anId + "-items");
        return locationData;
    }

    private DirectionData createGuardedDirection(String anId, String aDestinationId, String aCommandSpec,
                                                 String aRequiredItemId) {
        DirectionData directionData = new DirectionData();
        directionData.setId(anId);
        directionData.setDestinationId(aDestinationId);

        CommandData commandData = directionData.getCommandData();
        commandData.setCommandDescription(new CommandDescriptionData(aCommandSpec));
        CarriedConditionData carriedConditionData = new CarriedConditionData();
        carriedConditionData.setItemId(aRequiredItemId);
        commandData.getPreConditions().add(carriedConditionData);
        return directionData;
    }

    private CarriedCondition firstCarriedCondition(Adventure anAdventure, String aLocationId) {
        Location location = anAdventure.getLocations().stream()
                                       .filter(l -> aLocationId.equals(l.getId()))
                                       .findFirst()
                                       .orElseThrow();
        Direction direction = location.getDirections().getFirst();
        Command command = direction.getCommands().getFirst();
        return (CarriedCondition) command.getPreconditions().getFirst();
    }
}
