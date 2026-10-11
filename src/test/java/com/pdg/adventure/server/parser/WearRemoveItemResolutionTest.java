package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.action.RemoveAction;
import com.pdg.adventure.server.action.WearAction;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

/**
 * Item-level wear/remove commands (one WearAction/RemoveAction command per item, gated by Carried, as documented for
 * setUpWearCommands) must pick the same item AutoWear/AutoRemove would: both honour the pocket; only fitting items
 * count (wear: wearable and not yet worn; remove: worn); a typed adjective wins over the container; only a clash
 * inside the pocket is a question; identical items are one.
 */
class WearRemoveItemResolutionTest {

    private GameContext gameContext;
    private Container pocket;
    private Container room;
    private CommandExecutor sut;
    private Item blue;
    private Item neoprene;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
        gameContext.setPocket(pocket);
        room = new GenericContainer(new DescriptionProvider("room items"), 10);
        Location location = new Location(new DescriptionProvider("jetty", "end"), room);
        gameContext.setCurrentLocation(location);
        sut = new CommandExecutor(pocket, location);

        blue = suit("blue");
        neoprene = suit("neoprene");
    }

    private Item suit(String anAdjective) {
        Item item = new Item(new DescriptionProvider(anAdjective, "suit"), true);
        item.setIsWearable(true);
        GenericCommand wear = new GenericCommand(new GenericCommandDescription("wear"), new WearAction(item));
        wear.addPreCondition(new CarriedCondition(item, gameContext));
        item.addCommand(wear);
        GenericCommand remove = new GenericCommand(new GenericCommandDescription("remove"), new RemoveAction(item));
        remove.addPreCondition(new CarriedCondition(item, gameContext));
        item.addCommand(remove);
        return item;
    }

    private ExecutionResult wear(String anAdjective) {
        return sut.execute(new GenericCommandDescription("wear", anAdjective, "suit"));
    }

    private ExecutionResult remove(String anAdjective) {
        return sut.execute(new GenericCommandDescription("remove", anAdjective, "suit"));
    }

    private static String ambiguous(String aVerb) {
        return SystemMessageKey.SM61.defaultText().formatted("suit", aVerb);
    }

    // ---- wear: honours the pocket -----------------------------------------------------------------------------

    @Test
    void wear_oneCarriedOneInTheRoom_wearsTheCarriedOne() {
        pocket.add(blue);
        room.add(neoprene);

        ExecutionResult result = wear("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(blue.isWorn()).isTrue();
        assertThat(neoprene.isWorn()).isFalse();
    }

    @Test
    void wear_bothCarried_asksWhich() {
        pocket.add(blue);
        pocket.add(neoprene);

        assertThat(wear("").getResultMessage()).isEqualTo(ambiguous("wear"));
        assertThat(blue.isWorn()).isFalse();
        assertThat(neoprene.isWorn()).isFalse();
    }

    @Test
    void wear_bothInTheRoom_saysIDontHaveTheNoun() {
        room.add(blue);
        room.add(neoprene);

        ExecutionResult result = wear("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM49.defaultText().formatted("suit"));
    }

    @Test
    void wear_aWornAndAnUnwornCarriedSuit_wearsTheUnwornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = wear("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(neoprene.isWorn()).isTrue();
    }

    @Test
    void wear_theAdjectiveWinsOverTheHonouredContainer() {
        pocket.add(blue);
        room.add(neoprene);

        ExecutionResult result = wear("neoprene");

        assertThat(blue.isWorn()).isFalse();
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM49.defaultText().formatted("neoprene suit"));
    }

    @Test
    void wear_theAdjectiveResolvesAClashInThePocket() {
        pocket.add(blue);
        pocket.add(neoprene);

        wear("neoprene");

        assertThat(neoprene.isWorn()).isTrue();
        assertThat(blue.isWorn()).isFalse();
    }

    @Test
    void wear_twoIdenticalCarriedSuits_isNotAQuestion() {
        pocket.add(blue);
        pocket.add(suit("blue"));

        ExecutionResult result = wear("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    // ---- remove: honours the pocket ---------------------------------------------------------------------------

    @Test
    void remove_oneWornCarriedOneInTheRoom_removesTheWornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        room.add(neoprene);

        ExecutionResult result = remove("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(blue.isWorn()).isFalse();
    }

    @Test
    void remove_bothWorn_asksWhich() {
        blue.setIsWorn(true);
        neoprene.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        assertThat(remove("").getResultMessage()).isEqualTo(ambiguous("remove"));
        assertThat(blue.isWorn()).isTrue();
        assertThat(neoprene.isWorn()).isTrue();
    }

    @Test
    void remove_bothInTheRoom_saysImNotWearingTheNoun() {
        room.add(blue);
        room.add(neoprene);

        ExecutionResult result = remove("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM50.defaultText().formatted("suit"));
    }

    @Test
    void remove_wearingOneAndCarryingTheOther_removesTheWornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = remove("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(blue.isWorn()).isFalse();
    }

    @Test
    void remove_theAdjectiveWinsOverTheHonouredContainer() {
        blue.setIsWorn(true);
        pocket.add(blue);
        room.add(neoprene);

        ExecutionResult result = remove("neoprene");

        assertThat(blue.isWorn()).isTrue();
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM50.defaultText().formatted("neoprene suit"));
    }

    @Test
    void remove_bothCarriedNeitherWorn_saysImNotWearingTheNoun() {
        pocket.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = remove("");

        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM50.defaultText().formatted("suit"));
    }
}
