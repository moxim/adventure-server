package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.AutoDropAction;
import com.pdg.adventure.server.action.AutoRemoveAction;
import com.pdg.adventure.server.action.AutoTakeAction;
import com.pdg.adventure.server.action.AutoWearAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * Two items share the noun "suit" and the player types no distinguishing adjective: each Auto* action honours
 * one container first (AutoTake the location, AutoDrop/AutoWear/AutoRemove the pocket), and only a clash inside that
 * container is ambiguous. The other container is only consulted to explain why nothing happened.
 */
class AutoItemAmbiguityGameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GenericContainer pocket;
    private GenericContainer room;
    private Item blue;
    private Item neoprene;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
        gameContext.setPocket(pocket);
        room = new GenericContainer(new DescriptionProvider("room items"), 10);
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("jetty", "end"), room));

        blue = new Item(new DescriptionProvider("blue", "suit"), true);
        blue.setIsWearable(true);
        neoprene = new Item(new DescriptionProvider("neoprene", "suit"), true);
        neoprene.setIsWearable(true);

        Map<String, Item> allItems = new HashMap<>();
        allItems.put(blue.getId(), blue);
        allItems.put(neoprene.getId(), neoprene);

        Vocabulary vocabulary = new Vocabulary();
        for (String verb : new String[] {"take", "drop", "wear", "remove"}) {
            vocabulary.createNewWord(verb, Word.Type.VERB);
        }
        vocabulary.createNewWord("suit", Word.Type.NOUN);
        vocabulary.createNewWord("blue", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("neoprene", Word.Type.ADJECTIVE);

        Workflow workflow = gameContext.setUpWorkflows();
        respond(workflow, "take", new AutoTakeAction(gameContext, allItems));
        respond(workflow, "drop", new AutoDropAction(gameContext, allItems));
        respond(workflow, "wear", new AutoWearAction(gameContext, allItems));
        respond(workflow, "remove", new AutoRemoveAction(gameContext, allItems));

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    private static void respond(Workflow aWorkflow, String aVerb, Action anAction) {
        GenericCommandDescription description = new GenericCommandDescription(aVerb, "", VocabularyData.WILDCARD_NOUN);
        aWorkflow.addResponse(description, new GenericCommand(description, anAction));
    }

    private String ambiguous(String aVerb) {
        return SystemMessageKey.SM61.defaultText().formatted("suit", aVerb);
    }

    // ---- AutoTake: honours the location first -----------------------------------------------------------------

    @Test
    void take_oneInTheLocationOneInThePocket_takesTheOneInTheLocation() {
        room.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("take suit");

        assertThat(pocket.contains(blue)).isTrue();
        assertThat(room.contains(blue)).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM36.defaultText().formatted("blue suit"));
    }

    @Test
    void take_bothInThePocket_saysIAlreadyHaveTheNoun() {
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("take suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM25.defaultText().formatted("suit"));
    }

    @Test
    void take_oneInThePocketNothingInTheLocation_saysIAlreadyHaveThatItem() {
        pocket.add(blue);

        gameLoop.processCommand("take suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM25.defaultText().formatted("blue suit"));
    }

    @Test
    void take_bothInTheLocation_asksWhich() {
        room.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("take suit");

        assertThat(told.toString()).contains(ambiguous("take"));
        assertThat(room.contains(blue)).isTrue();
        assertThat(room.contains(neoprene)).isTrue();
    }

    @Test
    void take_theAdjectiveResolvesAnAmbiguityInTheLocation() {
        room.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("take neoprene suit");

        assertThat(pocket.contains(neoprene)).isTrue();
        assertThat(room.contains(blue)).isTrue();
    }

    @Test
    void take_theAdjectiveWinsOverTheHonouredContainer() {
        room.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("take neoprene suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM25.defaultText().formatted("neoprene suit"));
        assertThat(room.contains(blue)).isTrue();
    }

    @Test
    void take_twoIdenticalItemsAreNotAmbiguous() {
        Item first = new Item(new DescriptionProvider("blue", "suit"), true);
        Item second = new Item(new DescriptionProvider("blue", "suit"), true);
        room.add(first);
        room.add(second);

        gameLoop.processCommand("take suit");

        assertThat(told.toString()).doesNotContain(ambiguous("take"));
        assertThat(pocket.getContents()).hasSize(1);
        assertThat(room.getContents()).hasSize(1);
    }

    @Test
    void take_aContainableItemIsPreferredOverFixedScenerySharingTheNoun() {
        Item scenery = new Item(new DescriptionProvider("painted", "suit"), false);
        room.add(scenery);
        room.add(blue);

        gameLoop.processCommand("take suit");

        assertThat(pocket.contains(blue)).isTrue();
        assertThat(told.toString()).doesNotContain(ambiguous("take"));
    }

    @Test
    void take_ambiguityIsReportedBeforeAFullPocket() {
        for (int i = 0; i < 5; i++) { // the pocket holds five things
            pocket.add(new Item(new DescriptionProvider("stone" + i, "stone"), true));
        }
        room.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("take suit");

        assertThat(told.toString()).contains(ambiguous("take"))
                                   .doesNotContain(SystemMessageKey.SM27.defaultText());
    }

    // ---- AutoDrop: honours the pocket first -------------------------------------------------------------------

    @Test
    void drop_oneInThePocketOneInTheLocation_dropsTheOneInThePocket() {
        pocket.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("drop suit");

        assertThat(room.contains(blue)).isTrue();
        assertThat(pocket.contains(blue)).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM39.defaultText().formatted("blue suit"));
    }

    @Test
    void drop_bothInThePocket_asksWhich() {
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("drop suit");

        assertThat(told.toString()).contains(ambiguous("drop"));
        assertThat(pocket.contains(blue)).isTrue();
        assertThat(pocket.contains(neoprene)).isTrue();
    }

    @Test
    void drop_bothInTheLocation_saysIDontHaveTheNoun() {
        room.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("drop suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM49.defaultText().formatted("suit"));
    }

    @Test
    void drop_aWornAndAnUnwornCarriedItem_dropsTheUnwornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("drop suit");

        assertThat(room.contains(neoprene)).isTrue();
        assertThat(pocket.contains(blue)).isTrue();
    }

    @Test
    void drop_onlyAWornItemCarried_refuses() {
        blue.setIsWorn(true);
        pocket.add(blue);

        gameLoop.processCommand("drop suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM24.defaultText().formatted("blue suit"));
        assertThat(pocket.contains(blue)).isTrue();
    }

    // ---- AutoWear: honours the pocket first -------------------------------------------------------------------

    @Test
    void wear_oneInThePocketOneInTheLocation_wearsTheOneInThePocket() {
        pocket.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("wear suit");

        assertThat(blue.isWorn()).isTrue();
        assertThat(neoprene.isWorn()).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM37.defaultText().formatted("blue suit"));
    }

    @Test
    void wear_bothInThePocket_asksWhich() {
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("wear suit");

        assertThat(told.toString()).contains(ambiguous("wear"));
        assertThat(blue.isWorn()).isFalse();
        assertThat(neoprene.isWorn()).isFalse();
    }

    @Test
    void wear_bothInTheLocation_saysIDontHaveTheNoun() {
        room.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("wear suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM49.defaultText().formatted("suit"));
    }

    @Test
    void wear_aWornAndAnUnwornCarriedItem_wearsTheUnwornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("wear suit");

        assertThat(neoprene.isWorn()).isTrue();
    }

    @Test
    void wear_onlyAWornItemCarried_saysImAlreadyWearingIt() {
        blue.setIsWorn(true);
        pocket.add(blue);

        gameLoop.processCommand("wear suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM29.defaultText().formatted("blue suit"));
    }

    // ---- AutoRemove: honours the pocket first -----------------------------------------------------------------

    @Test
    void remove_oneWornInThePocketOneInTheLocation_removesTheWornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("remove suit");

        assertThat(blue.isWorn()).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM38.defaultText().formatted("blue suit"));
    }

    @Test
    void remove_twoWornInThePocket_asksWhich() {
        blue.setIsWorn(true);
        neoprene.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("remove suit");

        assertThat(told.toString()).contains(ambiguous("remove"));
        assertThat(blue.isWorn()).isTrue();
        assertThat(neoprene.isWorn()).isTrue();
    }

    @Test
    void remove_bothInTheLocation_saysImNotWearingTheNoun() {
        room.add(blue);
        room.add(neoprene);

        gameLoop.processCommand("remove suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM50.defaultText().formatted("suit"));
    }

    @Test
    void remove_wearingOneAndCarryingTheOther_removesTheWornOne() {
        blue.setIsWorn(true);
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("remove suit");

        assertThat(blue.isWorn()).isFalse();
        assertThat(told.toString()).doesNotContain(ambiguous("remove"));
    }

    @Test
    void remove_bothCarriedNeitherWorn_saysImNotWearingTheNoun() {
        pocket.add(blue);
        pocket.add(neoprene);

        gameLoop.processCommand("remove suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM50.defaultText().formatted("suit"));
    }
}
