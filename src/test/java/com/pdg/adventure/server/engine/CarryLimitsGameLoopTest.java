package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.AbilityAction;
import com.pdg.adventure.server.action.AutoTakeAction;
import com.pdg.adventure.server.action.TakeAction;
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
 * The player's ability (ABILITY): taking an item is refused, with SM27 or SM43, when the pocket already holds as many
 * objects as it may or the item would make the player carry too much - through AutoTake (typed noun) and Take
 * (a command built by hand on the item).
 */
class CarryLimitsGameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GameContext gameContext;
    private GameLoop gameLoop;
    private GenericContainer pocket;
    private GenericContainer roomItems;
    private Item anvil;
    private Item feather;
    private Item coin;
    private Item gem;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        pocket = new GenericContainer(new DescriptionProvider("pocket"), 10);
        gameContext.setPocket(pocket);
        roomItems = new GenericContainer(new DescriptionProvider("room items"), 10);
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("throne", "room"), roomItems));

        anvil = item("heavy", "anvil", 8);
        feather = item("white", "feather", 1);
        coin = item("gold", "coin", 0);
        gem = item("red", "gem", 2);
        Map<String, Item> allItems = new HashMap<>();
        for (Item item : new Item[] {anvil, feather, coin, gem}) {
            roomItems.add(item);
            allItems.put(item.getId(), item);
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("grab", Word.Type.VERB);
        for (String noun : new String[] {"anvil", "feather", "coin", "gem"}) {
            vocabulary.createNewWord(noun, Word.Type.NOUN);
        }

        Workflow workflow = gameContext.setUpWorkflows();
        workflow.addResponse(new GenericCommandDescription("take", "", VocabularyData.WILDCARD_NOUN),
                             new GenericCommand(new GenericCommandDescription("take", "", VocabularyData.WILDCARD_NOUN),
                                                new AutoTakeAction(gameContext, allItems)));
        // "grab gem" is a hand-built Take, held by the gem itself like the item editor's commands
        gem.addCommand(new GenericCommand(new GenericCommandDescription("grab"),
                                          new TakeAction(gem, new ContainerSupplier(gameContext::getPocket))));

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    private static Item item(String adjective, String noun, int weight) {
        Item item = new Item(new DescriptionProvider(adjective, noun), true);
        item.setWeight(weight);
        return item;
    }

    private void ability(int maxWeight, int maxItems) {
        new AbilityAction(maxWeight, maxItems, gameContext).execute();
    }

    @Test
    void withoutAnAbility_noWeightLimitApplies() {
        gameLoop.processCommand("take anvil");

        assertThat(pocket.contains(anvil)).isTrue();
    }

    @Test
    void autoTake_refusesAnItemThatIsTooHeavy_withSM43() {
        ability(5, 10);

        gameLoop.processCommand("take anvil");

        assertThat(told.toString()).contains(SystemMessageKey.SM43.defaultText().formatted("heavy anvil"));
        assertThat(pocket.contains(anvil)).isFalse();
        assertThat(roomItems.contains(anvil)).isTrue();
    }

    @Test
    void autoTake_acceptsAnItemThatBringsTheWeightExactlyToTheLimit() {
        ability(9, 10);
        gameLoop.processCommand("take anvil");
        gameLoop.processCommand("take feather");

        assertThat(pocket.contains(anvil)).isTrue();
        assertThat(pocket.contains(feather)).isTrue();
        assertThat(pocket.getCarriedWeight()).isEqualTo(9);
    }

    @Test
    void autoTake_refusesWhatPushesTheCarriedWeightOverTheLimit_butStillTakesALighterThing() {
        ability(9, 10);
        gameLoop.processCommand("take anvil");
        gameLoop.processCommand("take gem");
        gameLoop.processCommand("take coin");

        assertThat(told.toString()).contains(SystemMessageKey.SM43.defaultText().formatted("red gem"));
        assertThat(pocket.contains(gem)).isFalse();
        assertThat(pocket.contains(coin)).isTrue(); // weightless
    }

    @Test
    void autoTake_refusesWithSM27_whenThePlayerCarriesAsManyObjectsAsAllowed() {
        ability(100, 1);
        gameLoop.processCommand("take feather");
        gameLoop.processCommand("take coin");

        assertThat(told.toString()).contains(SystemMessageKey.SM27.defaultText());
        assertThat(pocket.contains(coin)).isFalse();
    }

    @Test
    void whenBothLimitsAreReached_theNumberIsReported() {
        ability(0, 0);

        gameLoop.processCommand("take anvil");

        assertThat(told.toString()).contains(SystemMessageKey.SM27.defaultText())
                                   .doesNotContain("weighs too much");
    }

    @Test
    void weightLimitZero_stillAllowsWeightlessItems() {
        ability(0, 10);

        gameLoop.processCommand("take coin");
        gameLoop.processCommand("take feather");

        assertThat(pocket.contains(coin)).isTrue();
        assertThat(pocket.contains(feather)).isFalse();
    }

    @Test
    void take_refusesAnItemThatIsTooHeavy_withSM43() {
        ability(1, 10);

        gameLoop.processCommand("grab gem");

        assertThat(told.toString()).contains(SystemMessageKey.SM43.defaultText().formatted("red gem"));
        assertThat(pocket.contains(gem)).isFalse();
    }

    @Test
    void take_refusesWithSM27_whenThePocketIsFull() {
        ability(100, 0);

        gameLoop.processCommand("grab gem");

        assertThat(told.toString()).contains(SystemMessageKey.SM27.defaultText());
        assertThat(pocket.contains(gem)).isFalse();
    }

    @Test
    void take_takesTheItemWithinTheLimits() {
        ability(2, 1);

        gameLoop.processCommand("grab gem");

        assertThat(pocket.contains(gem)).isTrue();
    }

    @Test
    void loweringTheAbility_dropsNothingTheyAlreadyCarry() {
        gameLoop.processCommand("take anvil");

        ability(1, 1);

        assertThat(pocket.contains(anvil)).isTrue();
    }
}
