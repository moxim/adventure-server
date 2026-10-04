package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.AutoDropAction;
import com.pdg.adventure.server.action.AutoTakeAction;
import com.pdg.adventure.server.action.MessageAction;
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
 * Drives "take ~ -> AUTOT" and "drop ~ -> AUTOD" responses through the real GameLoop, the way PAW's
 * "GET _ AUTOG" entry works: the items have no take/drop commands of their own, so the local
 * dispatch reports "nothing matched" and the response table's wildcard entry answers.
 */
class AutoTakeDropGameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GameContext gameContext;
    private Workflow workflow;
    private GameLoop gameLoop;
    private GenericContainer pocket;
    private GenericContainer roomItems;
    private Item apple;
    private Item lamp;
    private Item sword;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        pocket = new GenericContainer(new DescriptionProvider("pocket"), 2);
        gameContext.setPocket(pocket);
        roomItems = new GenericContainer(new DescriptionProvider("room items"), 10);
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("throne", "room"), roomItems));

        apple = new Item(new DescriptionProvider("red", "apple"), true);
        lamp = new Item(new DescriptionProvider("brass", "lamp"), true);
        sword = new Item(new DescriptionProvider("rusty", "sword"), true);
        Item statue = new Item(new DescriptionProvider("stone", "statue"), true);
        Item elsewhere = new Item(new DescriptionProvider("silver", "key"), true);
        roomItems.add(apple);
        roomItems.add(lamp);
        roomItems.add(statue);
        pocket.add(sword);

        Map<String, Item> allItems = new HashMap<>();
        for (Item item : new Item[] {apple, lamp, sword, statue, elsewhere}) {
            allItems.put(item.getId(), item);
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("drop", Word.Type.VERB);
        vocabulary.createNewWord("apple", Word.Type.NOUN);
        vocabulary.createNewWord("lamp", Word.Type.NOUN);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("key", Word.Type.NOUN);
        vocabulary.createNewWord("gate", Word.Type.NOUN); // a vocabulary word that is no object
        vocabulary.createNewWord("red", Word.Type.ADJECTIVE);

        workflow = gameContext.setUpWorkflows();
        workflow.addResponse(new GenericCommandDescription("take", "", VocabularyData.WILDCARD_NOUN),
                             new GenericCommand(new GenericCommandDescription("take", "", VocabularyData.WILDCARD_NOUN),
                                                new AutoTakeAction(gameContext, allItems)));
        workflow.addResponse(new GenericCommandDescription("drop", "", VocabularyData.WILDCARD_NOUN),
                             new GenericCommand(new GenericCommandDescription("drop", "", VocabularyData.WILDCARD_NOUN),
                                                new AutoDropAction(gameContext, allItems)));

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    @Test
    void take_movesTheNamedItemIntoThePocket() {
        gameLoop.processCommand("take apple");

        assertThat(pocket.contains(apple)).isTrue();
        assertThat(roomItems.contains(apple)).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM36.defaultText().formatted("red apple"));
    }

    @Test
    void take_ofAnItemAlreadyCarried_saysSoAndChangesNothing() {
        gameLoop.processCommand("take sword");

        assertThat(told.toString()).contains(SystemMessageKey.SM25.defaultText().formatted("rusty sword"));
        assertThat(pocket.contains(sword)).isTrue();
    }

    @Test
    void take_ofAnItemInAnotherLocation_saysThereIsNoneHere() {
        gameLoop.processCommand("take key");

        assertThat(told.toString()).contains(SystemMessageKey.SM26.defaultText());
    }

    @Test
    void take_ofAVocabularyWordThatIsNoObject_saysICantDoThat() {
        gameLoop.processCommand("take gate");

        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void take_ofAWordTheParserDoesNotKnow_isTreatedLikeAMissingObject() {
        gameLoop.processCommand("take xyzzy");

        assertThat(told.toString()).contains(SystemMessageKey.SM26.defaultText());
    }

    @Test
    void take_whenThePocketIsFull_saysICantCarryMore() {
        gameLoop.processCommand("take apple");
        gameLoop.processCommand("take lamp");

        assertThat(told.toString()).contains(SystemMessageKey.SM27.defaultText());
        assertThat(roomItems.contains(lamp)).isTrue();
    }

    @Test
    void take_usesTheAdjectiveWhenGiven() {
        gameLoop.processCommand("take red apple");

        assertThat(pocket.contains(apple)).isTrue();
    }

    @Test
    void drop_movesTheNamedCarriedItemIntoTheLocation() {
        gameLoop.processCommand("drop sword");

        assertThat(roomItems.contains(sword)).isTrue();
        assertThat(pocket.contains(sword)).isFalse();
        assertThat(told.toString()).contains(SystemMessageKey.SM39.defaultText().formatted("rusty sword"));
    }

    @Test
    void drop_ofAWornItem_refuses() {
        sword.setIsWorn(true);

        gameLoop.processCommand("drop sword");

        assertThat(told.toString()).contains(SystemMessageKey.SM24.defaultText().formatted("rusty sword"));
        assertThat(pocket.contains(sword)).isTrue();
    }

    @Test
    void drop_ofAnItemThatIsHereButNotCarried_saysIDontHaveIt() {
        gameLoop.processCommand("drop apple");

        assertThat(told.toString()).contains(SystemMessageKey.SM49.defaultText().formatted("red apple"));
        assertThat(roomItems.contains(apple)).isTrue();
    }

    @Test
    void drop_ofAnItemElsewhere_saysIDontHaveOneOfThose() {
        gameLoop.processCommand("drop key");

        assertThat(told.toString()).contains(SystemMessageKey.SM28.defaultText());
    }

    @Test
    void drop_ofAVocabularyWordThatIsNoObject_saysICantDoThat() {
        gameLoop.processCommand("drop gate");

        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void drop_withNoKnownNoun_saysIDontHaveOneOfThose() {
        gameLoop.processCommand("drop xyzzy");

        assertThat(told.toString()).contains(SystemMessageKey.SM28.defaultText());
    }

    @Test
    void anExactResponse_beatsTheWildcardOne() {
        GenericCommandDescription exact = new GenericCommandDescription("take", "", "lamp");
        workflow.addResponse(exact, new GenericCommand(exact, new MessageAction("The lamp is bolted down.")));

        gameLoop.processCommand("take lamp");

        assertThat(told.toString()).contains("The lamp is bolted down.");
        assertThat(pocket.contains(lamp)).isFalse();
    }
}
