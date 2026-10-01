package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.AutoRemoveAction;
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

/** Drives "wear ~ -> AUTOW" and "remove ~ -> AUTOR" responses through the real GameLoop. */
class AutoWearRemoveGameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GameLoop gameLoop;
    private GenericContainer pocket;
    private GenericContainer roomItems;
    private Item suit;
    private Item hat;
    private Item cloak;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 10);
        gameContext.setPocket(pocket);
        roomItems = new GenericContainer(new DescriptionProvider("room items"), 10);
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("throne", "room"), roomItems));

        suit = new Item(new DescriptionProvider("neoprene", "suit"), true);
        suit.setIsWearable(true);
        hat = new Item(new DescriptionProvider("old", "hat"), true);
        hat.setIsWearable(true);
        Item spanner = new Item(new DescriptionProvider("old", "spanner"), true);
        cloak = new Item(new DescriptionProvider("grey", "cloak"), true);
        cloak.setIsWearable(true);
        Item elsewhere = new Item(new DescriptionProvider("silver", "ring"), true);
        pocket.add(suit);
        pocket.add(spanner);
        pocket.add(hat);
        hat.setIsWorn(true);
        roomItems.add(cloak);

        Map<String, Item> allItems = new HashMap<>();
        for (Item item : new Item[] {suit, hat, spanner, cloak, elsewhere}) {
            allItems.put(item.getId(), item);
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("wear", Word.Type.VERB);
        vocabulary.createNewWord("remove", Word.Type.VERB);
        for (String noun : new String[] {"suit", "hat", "spanner", "cloak", "ring", "gate"}) {
            vocabulary.createNewWord(noun, Word.Type.NOUN);
        }

        Workflow workflow = gameContext.setUpWorkflows();
        GenericCommandDescription wear = new GenericCommandDescription("wear", "", VocabularyData.WILDCARD_NOUN);
        workflow.addResponse(wear, new GenericCommand(wear, new AutoWearAction(gameContext, allItems)));
        GenericCommandDescription remove = new GenericCommandDescription("remove", "", VocabularyData.WILDCARD_NOUN);
        workflow.addResponse(remove, new GenericCommand(remove, new AutoRemoveAction(gameContext, allItems)));

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    @Test
    void wear_putsOnTheNamedCarriedItem() {
        gameLoop.processCommand("wear suit");

        assertThat(suit.isWorn()).isTrue();
        assertThat(told.toString()).contains(SystemMessageKey.SM37.defaultText().formatted("neoprene suit"));
    }

    @Test
    void wear_ofAnItemAlreadyWorn_saysSo() {
        gameLoop.processCommand("wear hat");

        assertThat(told.toString()).contains(SystemMessageKey.SM29.defaultText().formatted("old hat"));
    }

    @Test
    void wear_ofACarriedItemThatCannotBeWorn_refuses() {
        gameLoop.processCommand("wear spanner");

        assertThat(told.toString()).contains(SystemMessageKey.SM40.defaultText().formatted("old spanner"));
    }

    @Test
    void wear_ofAnItemThatIsHereButNotCarried_saysIDontHaveIt() {
        gameLoop.processCommand("wear cloak");

        assertThat(told.toString()).contains(SystemMessageKey.SM49.defaultText().formatted("grey cloak"));
        assertThat(cloak.isWorn()).isFalse();
    }

    @Test
    void wear_ofAnItemElsewhere_orNoNoun_saysIDontHaveOneOfThose() {
        gameLoop.processCommand("wear ring");
        gameLoop.processCommand("wear xyzzy");

        assertThat(told.toString().split(SystemMessageKey.SM28.defaultText(), -1)).hasSize(3);
    }

    @Test
    void wear_ofAVocabularyWordThatIsNoObject_saysICantDoThat() {
        gameLoop.processCommand("wear gate");

        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void remove_takesOffTheWornItem_andKeepsItInThePocket() {
        gameLoop.processCommand("remove hat");

        assertThat(hat.isWorn()).isFalse();
        assertThat(pocket.contains(hat)).isTrue();
        assertThat(told.toString()).contains(SystemMessageKey.SM38.defaultText().formatted("old hat"));
    }

    @Test
    void remove_ofACarriedItemThatIsNotWorn_saysIAmNotWearingIt() {
        gameLoop.processCommand("remove suit");

        assertThat(told.toString()).contains(SystemMessageKey.SM50.defaultText().formatted("neoprene suit"));
    }

    @Test
    void remove_ofAnItemHereButNotCarried_saysIAmNotWearingIt() {
        gameLoop.processCommand("remove cloak");

        assertThat(told.toString()).contains(SystemMessageKey.SM50.defaultText().formatted("grey cloak"));
    }

    @Test
    void remove_ofAnItemElsewhere_orNoNoun_saysIAmNotWearingAnyOfThose() {
        gameLoop.processCommand("remove ring");
        gameLoop.processCommand("remove xyzzy");

        assertThat(told.toString().split(SystemMessageKey.SM23.defaultText().replace(".", "\\."), -1)).hasSize(3);
    }

    @Test
    void remove_ofAVocabularyWordThatIsNoObject_saysICantDoThat() {
        gameLoop.processCommand("remove gate");

        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
    }
}
