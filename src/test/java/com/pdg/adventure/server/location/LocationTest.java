package com.pdg.adventure.server.location;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Direction;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.testhelper.TestSupporter;

class LocationTest {

    private final Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);
    private final Map<String, Location> allLocations = new HashMap<>();
    private final Location sut = new Location(new DescriptionProvider("small", "perch"), pocket);

    {
        allLocations.put(sut.getId(), sut);
    }

    private final Direction direction = new GenericDirection(allLocations,
                                                             new GenericCommand(
                                                                     new GenericCommandDescription("loop"), null),
                                                             sut.getId());
    private final Item mouse = new Item(new DescriptionProvider("mouse"), true);

    @Test
    void addItem() {
        // given

        // when
        boolean success = TestSupporter.addItemToBoolean(sut.getItemContainer(), mouse);

        // then
        assertThat(success).isTrue();
        assertThat(mouse.getParentContainer()).isEqualTo(sut.getItemContainer());
        assertThat(sut.contains(mouse)).isTrue();
    }

    @Test
    void removeItemFailsIfItemIsNotFound() throws Exception {
        // given
        assertThat(sut.contains(mouse)).isFalse();

        // when
        boolean success = TestSupporter.removeItemToBoolean(sut.getItemContainer(), mouse);

        // then
        assertThat(success).isFalse();
        assertThat(sut.contains(mouse)).isFalse();
    }

    @Test
    void removeItemSucceedsIfItemIsFound() throws Exception {
        // given
        sut.addItem(mouse);

        // when
        boolean success = TestSupporter.removeItemToBoolean(sut.getItemContainer(), mouse);

        // then
        assertThat(success).isTrue();
        assertThat(sut.contains(mouse)).isFalse();
    }

    @Test
    void addDirection() {
        // given
        // direction is already created in test setup

        // when
        ExecutionResult result = sut.addDirection(direction);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(sut.getDirections()).containsExactly((GenericDirection) direction);
    }

    @Test
    void applyCommand() {
        // given
        // No direction added, so no commands will match
        GenericCommandDescription commandDescription = new GenericCommandDescription("nonexistent");

        // when
        ExecutionResult result = sut.applyCommand(commandDescription);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void describeItemAtLocation_returnsOnlyTheItemChain_notTheLocationFallback() {
        // given: the location has an examine fallback (verb "describe") and holds an item
        // with its own real "describe basket" command.
        sut.setExamineFallback("describe", sut::getLongDescription);
        Item basket = new Item(new DescriptionProvider("basket"), true);
        basket.addCommand(new GenericCommand(new GenericCommandDescription("describe", basket),
                                             new MessageAction("A wicker basket.")));
        sut.addItem(basket);

        // when: the user asks to describe the basket
        List<CommandChain> chains = sut.getMatchingCommandChain(
                new GenericCommandDescription("describe", "basket"));

        // then: only the basket's chain is returned — the location's fallback must not fire
        assertThat(chains).hasSize(1);
        assertThat(chains.getFirst().execute().getResultMessage()).isEqualTo("A wicker basket.");
    }

    @Test
    void describeItemWithAdjective_resolvesToMatchingItem() {
        // given: the location has an examine fallback and holds a "short sword"
        // (adjective=short, noun=sword) with a real describe command.
        sut.setExamineFallback("describe", sut::getLongDescription);
        Item shortSword = new Item(new DescriptionProvider("short", "sword"), true);
        shortSword.addCommand(new GenericCommand(new GenericCommandDescription("describe", shortSword),
                                                 new MessageAction("A short sword.")));
        sut.addItem(shortSword);

        // when: the user asks to describe the short sword
        List<CommandChain> chains = sut.getMatchingCommandChain(
                new GenericCommandDescription("describe", "short", "sword"));

        // then: only the short sword's chain is returned
        assertThat(chains).hasSize(1);
        assertThat(chains.getFirst().execute().getResultMessage()).isEqualTo("A short sword.");
    }

    @Test
    void getLongDescription() {
        // given
        sut.addItem(mouse);
        sut.addDirection(direction);

        // when
        String desc = sut.getLongDescription();

        // then
        assertThat(desc).contains(List.of("small", "perch", "mouse", "loop"));
    }

    @Test
    void getArrivalDescription_onFirstVisit_usesTheLongDescription() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        assertThat(room.getTimesVisited()).isZero();

        assertThat(room.getArrivalDescription())
                .contains("The long, richly detailed room.")
                .doesNotContain("The short room.");
    }

    @Test
    void getArrivalDescription_afterTheFirstVisit_usesTheShortDescription() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        room.setTimesVisited(1);

        assertThat(room.getArrivalDescription())
                .contains("The short room.")
                .doesNotContain("The long, richly detailed room.");
    }

    @Test
    void getLongDescription_alwaysUsesTheLongDescription_evenAfterRepeatedVisits() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        room.setTimesVisited(5);

        assertThat(room.getLongDescription())
                .contains("The long, richly detailed room.")
                .doesNotContain("The short room.");
    }

    private Location roomWithDescriptions(String aShortDescription, String aLongDescription) {
        DescriptionProvider descriptionProvider = new DescriptionProvider("plain", "room");
        descriptionProvider.setShortDescription(aShortDescription);
        descriptionProvider.setLongDescription(aLongDescription);
        return new Location(descriptionProvider, new GenericContainer(new DescriptionProvider("room items"), 5));
    }

    @Test
    void getLongDescription_belowTheLightThreshold_yieldsOnlyTheDarknessMessage() {
        sut.addItem(mouse);
        sut.addDirection(direction);
        sut.setLight(9);

        String desc = sut.getLongDescription();

        assertThat(desc).isEqualTo(System.lineSeparator() + SystemMessageKey.SM0.defaultText());
        assertThat(desc).doesNotContain("small", "perch", "mouse", "loop");
    }

    @Test
    void getLongDescription_atExactlyTheLightThreshold_isNotDark() {
        sut.setLight(10);

        String desc = sut.getLongDescription();

        assertThat(desc).doesNotContain(SystemMessageKey.SM0.defaultText());
    }

    @Test
    void getArrivalDescription_belowTheLightThreshold_yieldsOnlyTheDarknessMessage() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        room.setLight(0);

        assertThat(room.getArrivalDescription())
                .isEqualTo(System.lineSeparator() + SystemMessageKey.SM0.defaultText())
                .doesNotContain("The long, richly detailed room.");
    }

    @Test
    void newLocation_defaultsToLit() {
        assertThat(sut.getPerceivedLight()).isEqualTo(50);
        assertThat(sut.getLongDescription()).doesNotContain(SystemMessageKey.SM0.defaultText());
    }

    @Test
    void getPerceivedLight_addsTheLumenOfItemsPresentInTheLocation() {
        mouse.setLight(20);
        sut.addItem(mouse);

        assertThat(sut.getPerceivedLight()).isEqualTo(70); // 50 ambient + 20 from the item
    }

    @Test
    void getPerceivedLight_addsTheLumenOfCarriedItems() {
        Item torch = new Item(new DescriptionProvider("torch"), true);
        torch.setLight(15);
        Container carried = new GenericContainer(new DescriptionProvider("carried items"), 5);
        carried.add(torch);
        sut.setCarriedItems(carried);

        assertThat(sut.getPerceivedLight()).isEqualTo(65); // 50 ambient + 15 carried
    }

    @Test
    void getPerceivedLight_sumsAmbientLocationAndCarriedLight() {
        mouse.setLight(5);
        sut.addItem(mouse);

        Item torch = new Item(new DescriptionProvider("torch"), true);
        torch.setLight(15);
        Container carried = new GenericContainer(new DescriptionProvider("carried items"), 5);
        carried.add(torch);
        sut.setCarriedItems(carried);

        assertThat(sut.getPerceivedLight()).isEqualTo(70); // 50 ambient + 5 item + 15 carried
    }

    @Test
    void getPerceivedLight_withNoCarriedItemsContainerSet_ignoresIt() {
        // setCarriedItems() is only wired up once an adventure is actually loaded; a Location
        // built directly (as most tests do) must not NPE for lacking one.
        assertThat(sut.getPerceivedLight()).isEqualTo(50);
    }

    @Test
    void getLongDescription_aCarriedTorchCanLightUpAnOtherwiseDarkLocation() {
        sut.addItem(mouse);
        sut.addDirection(direction);
        sut.setLight(0);

        Item torch = new Item(new DescriptionProvider("torch"), true);
        torch.setLight(15);
        Container carried = new GenericContainer(new DescriptionProvider("carried items"), 5);
        carried.add(torch);
        sut.setCarriedItems(carried);

        String desc = sut.getLongDescription();

        assertThat(desc).doesNotContain(SystemMessageKey.SM0.defaultText())
                        .contains("small", "perch", "mouse", "loop");
    }
}
