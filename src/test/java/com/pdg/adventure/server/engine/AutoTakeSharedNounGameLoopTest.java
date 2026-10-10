package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
 * Mirrors the demo adventure's two suits: they share the noun "suit" and differ only in the
 * adjective, and the player handles them through the "take/drop/wear/remove ~" -> Auto* responses.
 * A typed adjective must never be satisfied by the other suit when the question is "is it
 * carried/worn?".
 */
class AutoTakeSharedNounGameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GenericContainer pocket;
    private GenericContainer roomItems;
    private Item blueSuit;
    private Item neopreneSuit;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
        gameContext.setPocket(pocket);
        roomItems = new GenericContainer(new DescriptionProvider("room items"), 10);
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("jetty", "end"), roomItems));

        blueSuit = new Item(new DescriptionProvider("blue", "suit"), true);
        blueSuit.setIsWearable(true);
        neopreneSuit = new Item(new DescriptionProvider("neoprene", "suit"), true);
        neopreneSuit.setIsWearable(true);
        roomItems.add(blueSuit);
        roomItems.add(neopreneSuit);

        Map<String, Item> allItems = new HashMap<>();
        allItems.put(blueSuit.getId(), blueSuit);
        allItems.put(neopreneSuit.getId(), neopreneSuit);

        Vocabulary vocabulary = new Vocabulary();
        for (String verb : new String[] {"take", "drop", "wear", "remove"}) {
            vocabulary.createNewWord(verb, Word.Type.VERB);
        }
        vocabulary.createNewWord("suit", Word.Type.NOUN);
        vocabulary.createNewWord("blue", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("neoprene", Word.Type.ADJECTIVE);

        Workflow workflow = gameContext.setUpWorkflows();
        addWildcardResponse(workflow, "take", new AutoTakeAction(gameContext, allItems));
        addWildcardResponse(workflow, "drop", new AutoDropAction(gameContext, allItems));
        addWildcardResponse(workflow, "wear", new AutoWearAction(gameContext, allItems));
        addWildcardResponse(workflow, "remove", new AutoRemoveAction(gameContext, allItems));

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    private static void addWildcardResponse(Workflow aWorkflow, String aVerb, com.pdg.adventure.api.Action anAction) {
        GenericCommandDescription description = new GenericCommandDescription(aVerb, "", VocabularyData.WILDCARD_NOUN);
        aWorkflow.addResponse(description, new GenericCommand(description, anAction));
    }

    private void carryBlueSuit() {
        roomItems.remove(blueSuit);
        pocket.add(blueSuit);
    }

    @Test
    void takingTheBlueSuitThenTheNeopreneSuit_picksUpBoth() {
        gameLoop.processCommand("take blue suit");
        gameLoop.processCommand("take neoprene suit");

        assertThat(pocket.contains(blueSuit)).isTrue();
        assertThat(pocket.contains(neopreneSuit)).as("told: %s", told).isTrue();
        assertThat(roomItems.contains(neopreneSuit)).isFalse();
        assertThat(told.toString()).doesNotContain(SystemMessageKey.SM25.defaultText().formatted("blue suit"));
    }

    @Test
    void dropNeopreneSuit_whileOnlyTheBlueSuitIsCarried_dropsNothing() {
        carryBlueSuit();

        gameLoop.processCommand("drop neoprene suit");

        assertThat(pocket.contains(blueSuit)).isTrue();
        assertThat(told.toString()).contains(SystemMessageKey.SM49.defaultText().formatted("neoprene suit"));
    }

    @Test
    void dropBlueSuit_whileCarryingIt_stillDropsIt() {
        carryBlueSuit();

        gameLoop.processCommand("drop blue suit");

        assertThat(roomItems.contains(blueSuit)).isTrue();
        assertThat(pocket.contains(blueSuit)).isFalse();
    }

    @Test
    void dropSuit_withoutAdjective_stillDropsTheCarriedOne() {
        carryBlueSuit();

        gameLoop.processCommand("drop suit");

        assertThat(roomItems.contains(blueSuit)).isTrue();
    }

    @Test
    void wearNeopreneSuit_whileOnlyTheBlueSuitIsCarried_wearsNothing() {
        carryBlueSuit();

        gameLoop.processCommand("wear neoprene suit");

        assertThat(blueSuit.isWorn()).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM49.defaultText().formatted("neoprene suit"));
    }

    @Test
    void wearBlueSuit_whileCarryingIt_stillWearsIt() {
        carryBlueSuit();

        gameLoop.processCommand("wear blue suit");

        assertThat(blueSuit.isWorn()).isTrue();
    }

    @Test
    void removeNeopreneSuit_whileOnlyTheBlueSuitIsWorn_removesNothing() {
        carryBlueSuit();
        blueSuit.setIsWorn(true);

        gameLoop.processCommand("remove neoprene suit");

        assertThat(blueSuit.isWorn()).isTrue();
        assertThat(told.toString()).contains(SystemMessageKey.SM50.defaultText().formatted("neoprene suit"));
    }

    @Test
    void removeBlueSuit_whileWearingIt_stillRemovesIt() {
        carryBlueSuit();
        blueSuit.setIsWorn(true);

        gameLoop.processCommand("remove blue suit");

        assertThat(blueSuit.isWorn()).isFalse();
    }
}
