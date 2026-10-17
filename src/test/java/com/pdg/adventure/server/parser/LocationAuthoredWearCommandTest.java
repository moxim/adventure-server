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
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

/**
 * An author may put the wear/remove command on a <i>location</i> ("wear neoprene suit", Carried a neoprene suit -> Wear
 * a neoprene suit), as The Demo does at the west dunes, instead of on the item. Such a command must work for a carried
 * item: the item resolver only decides between several item-level commands, it must not refuse a location's one.
 */
class LocationAuthoredWearCommandTest {

    private Container pocket;
    private Container room;
    private GameContext gameContext;
    private Location location;
    private CommandExecutor sut;
    private Item neoprene;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
        gameContext.setPocket(pocket);
        room = new GenericContainer(new DescriptionProvider("room items"), 10);
        location = new Location(new DescriptionProvider("west dunes"), room);
        gameContext.setCurrentLocation(location);
        sut = new CommandExecutor(pocket, location);

        neoprene = new Item(new DescriptionProvider("neoprene", "suit"), true);
        neoprene.setIsWearable(true);
    }

    private void authorOnTheLocation(String aVerb, boolean aWear) {
        GenericCommand command = new GenericCommand(new GenericCommandDescription(aVerb, "neoprene", "suit"),
                                                    aWear ? new WearAction(neoprene) : new RemoveAction(neoprene));
        command.addPreCondition(new CarriedCondition(neoprene, gameContext));
        location.addCommand(command);
    }

    @Test
    void wear_aCarriedItem_viaTheLocationsCommand_wearsIt() {
        authorOnTheLocation("wear", true);
        pocket.add(neoprene);

        ExecutionResult result = sut.execute(new GenericCommandDescription("wear", "neoprene", "suit"));

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(neoprene.isWorn()).isTrue();
    }

    @Test
    void remove_aWornItem_viaTheLocationsCommand_removesIt() {
        authorOnTheLocation("remove", false);
        neoprene.setIsWorn(true);
        pocket.add(neoprene);

        ExecutionResult result = sut.execute(new GenericCommandDescription("remove", "neoprene", "suit"));

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(neoprene.isWorn()).isFalse();
    }
}
