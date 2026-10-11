package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.action.DropAction;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.TakeAction;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

/**
 * Item-level take/drop commands (a TakeAction/DropAction chain on each item) must pick the same item the AUTOT/AUTOD
 * actions would: take honours the location, drop the pocket; a typed adjective wins over the container; only a clash
 * inside the honoured container is a question; identical items are one.
 */
class TakeDropItemResolutionTest {

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

    /** A suit with the three take variants and two drop variants the item editor used to generate. */
    private Item suit(String anAdjective) {
        Item item = new Item(new DescriptionProvider(anAdjective, "suit"), true);

        GenericCommandDescription take = new GenericCommandDescription("take");
        GenericCommand alreadyCarried = new GenericCommand(take, new MessageAction("already_carried"));
        alreadyCarried.addPreCondition(new CarriedCondition(item, gameContext));
        item.addCommand(alreadyCarried);
        GenericCommand notHere = new GenericCommand(take, new MessageAction("not_here"));
        notHere.addPreCondition(new NotCondition(new HereCondition(item, gameContext)));
        item.addCommand(notHere);
        GenericCommand realTake = new GenericCommand(take, new TakeAction(item, new ContainerSupplier(() -> pocket)));
        realTake.addPreCondition(new HereCondition(item, gameContext));
        item.addCommand(realTake);

        GenericCommandDescription drop = new GenericCommandDescription("drop");
        GenericCommand notCarried = new GenericCommand(drop, new MessageAction("not_carried"));
        notCarried.addPreCondition(new NotCondition(new CarriedCondition(item, gameContext)));
        item.addCommand(notCarried);
        item.addCommand(new GenericCommand(drop, new DropAction(item, new ContainerSupplier(() -> room))));
        return item;
    }

    private ExecutionResult take(String anAdjective) {
        return sut.execute(new GenericCommandDescription("take", anAdjective, "suit"));
    }

    private ExecutionResult drop(String anAdjective) {
        return sut.execute(new GenericCommandDescription("drop", anAdjective, "suit"));
    }

    private static String ambiguous(String aVerb) {
        return SystemMessageKey.SM61.defaultText().formatted("suit", aVerb);
    }

    // ---- take: honours the location ---------------------------------------------------------------------------

    @Test
    void take_oneInTheRoomOneCarried_takesTheOneInTheRoom() {
        room.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = take("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(pocket.contains(blue)).isTrue();
        assertThat(room.contains(blue)).isFalse();
    }

    @Test
    void take_bothCarried_saysIAlreadyHaveTheNoun() {
        pocket.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = take("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM25.defaultText().formatted("suit"));
    }

    @Test
    void take_bothInTheRoom_asksWhich() {
        room.add(blue);
        room.add(neoprene);

        ExecutionResult result = take("");

        assertThat(result.getResultMessage()).isEqualTo(ambiguous("take"));
        assertThat(room.contains(blue)).isTrue();
        assertThat(room.contains(neoprene)).isTrue();
    }

    @Test
    void take_theAdjectiveWinsOverTheHonouredContainer() {
        room.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = take("neoprene");

        assertThat(result.getResultMessage()).contains("already_carried");
        assertThat(room.contains(blue)).isTrue();
    }

    @Test
    void take_theAdjectiveResolvesAClashInTheRoom() {
        room.add(blue);
        room.add(neoprene);

        take("neoprene");

        assertThat(pocket.contains(neoprene)).isTrue();
        assertThat(room.contains(blue)).isTrue();
    }

    @Test
    void take_twoIdenticalSuitsInTheRoom_isNotAQuestion() {
        room.add(blue);
        room.add(suit("blue"));

        ExecutionResult result = take("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(pocket.getContents()).hasSize(1);
        assertThat(room.getContents()).hasSize(1);
    }

    @Test
    void take_onlyOneCarriedNothingInTheRoom_theItemsOwnChainAnswers() {
        pocket.add(blue);

        assertThat(take("").getResultMessage()).contains("already_carried");
    }

    // ---- drop: honours the pocket -----------------------------------------------------------------------------

    @Test
    void drop_oneCarriedOneInTheRoom_dropsTheCarriedOne() {
        pocket.add(blue);
        room.add(neoprene);

        ExecutionResult result = drop("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(room.contains(blue)).isTrue();
        assertThat(pocket.contains(blue)).isFalse();
    }

    @Test
    void drop_bothCarried_asksWhich() {
        pocket.add(blue);
        pocket.add(neoprene);

        ExecutionResult result = drop("");

        assertThat(result.getResultMessage()).isEqualTo(ambiguous("drop"));
        assertThat(pocket.contains(blue)).isTrue();
        assertThat(pocket.contains(neoprene)).isTrue();
    }

    @Test
    void drop_bothInTheRoom_saysIDontHaveTheNoun() {
        room.add(blue);
        room.add(neoprene);

        ExecutionResult result = drop("");

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM49.defaultText().formatted("suit"));
    }

    @Test
    void drop_theAdjectiveWinsOverTheHonouredContainer() {
        pocket.add(blue);
        room.add(neoprene);

        ExecutionResult result = drop("neoprene");

        assertThat(result.getResultMessage()).contains("not_carried");
        assertThat(pocket.contains(blue)).isTrue();
    }
}
