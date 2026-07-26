package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.WearAction;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandChain;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

/**
 * Reproduces the "suit machine" scenario: a location offers two "wear suit" commands —
 * one that wears the neoprene suit (guarded by CARRIED + NOT_WORN neoprene suit) and one
 * that prints a message (guarded by NOT_HERE + NOT_CARRIED neoprene suit + CARRIED swim suit).
 * <p>
 * Both suits share the noun "suit" and have no distinguishing adjective, so conditions must
 * match items by identity, not by description: carrying only the swim suit must NOT satisfy
 * "CARRIED a neoprene suit".
 */
class WearMachineScenarioTest {

    private static final String MACHINE_CANNOT_HELP = "machine_cannot_help";

    private final MessagesHolder messages = new MessagesHolder();
    private GameContext gameContext;
    private Item neopreneSuit;
    private Item swimSuit;
    private GenericCommandChain wearSuitChain;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();

        neopreneSuit = createSuit("neoprene suit");
        swimSuit = createSuit("swim suit");

        GenericContainer pocket = new GenericContainer(new DescriptionProvider("pocket"), 10);
        gameContext.setPocket(pocket);

        GenericContainer machineRoomItems = new GenericContainer(new DescriptionProvider("machine room items"), 10);
        Location machineRoom = new Location(new DescriptionProvider("machine room"), machineRoomItems);
        gameContext.setCurrentLocation(machineRoom);

        // the location's "wear||suit" command chain, exactly as authored in the editor
        GenericCommandDescription wearSuit = new GenericCommandDescription("wear", "", "suit");

        GenericCommand wearNeoprene = new GenericCommand(wearSuit, new WearAction(neopreneSuit, messages));
        wearNeoprene.addPreCondition(new CarriedCondition(neopreneSuit, gameContext));
        wearNeoprene.addPreCondition(new NotCondition(new WornCondition(neopreneSuit)));

        GenericCommand machineCannotHelp = new GenericCommand(wearSuit, new MessageAction(MACHINE_CANNOT_HELP,
                                                                                          messages));
        machineCannotHelp.addPreCondition(new NotCondition(new HereCondition(neopreneSuit, gameContext)));
        machineCannotHelp.addPreCondition(new NotCondition(new CarriedCondition(neopreneSuit, gameContext)));
        machineCannotHelp.addPreCondition(new CarriedCondition(swimSuit, gameContext));

        wearSuitChain = new GenericCommandChain();
        wearSuitChain.addCommand(wearNeoprene);
        wearSuitChain.addCommand(machineCannotHelp);
    }

    @Test
    void carryingOnlyTheSwimSuit_printsMachineMessage_andDoesNotWearRemoteNeopreneSuit() {
        // given: only the swim suit is carried; the neoprene suit is in another location
        gameContext.getPocket().add(swimSuit);

        // when
        ExecutionResult result = wearSuitChain.execute();

        // then
        assertThat(neopreneSuit.isWorn())
                .as("the neoprene suit is elsewhere and must not become worn")
                .isFalse();
        assertThat(result.getResultMessage()).contains(MACHINE_CANNOT_HELP);
    }

    @Test
    void carryingTheNeopreneSuit_wearsIt_andSkipsMachineMessage() {
        // given
        gameContext.getPocket().add(neopreneSuit);

        // when
        ExecutionResult result = wearSuitChain.execute();

        // then
        assertThat(neopreneSuit.isWorn()).isTrue();
        assertThat(result.getResultMessage()).doesNotContain(MACHINE_CANNOT_HELP);
    }

    private Item createSuit(String aShortDescription) {
        DescriptionProvider description = new DescriptionProvider("suit");
        description.setShortDescription(aShortDescription);
        Item suit = new Item(description, true);
        suit.setIsWearable(true);
        return suit;
    }
}
