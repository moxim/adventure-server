package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.GameSnapshotData;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadGameAction;
import com.pdg.adventure.server.action.SaveGameAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.repository.SavedGameRepository;
import com.pdg.adventure.server.storage.service.SavedGameService;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/** Drives "save ~ -> SaveGameAction" and "load ~ -> LoadGameAction" responses through the real GameLoop. */
class SaveLoadGameLoopTest {

    private static final String PLAYER = "player-1";
    private static final String ADVENTURE = "adv-1";

    private final StringBuilder told = new StringBuilder();
    private final Map<String, SavedGameData> store = new HashMap<>();
    private GameContext gameContext;
    private GameLoop gameLoop;
    private GenericContainer pocket;
    private GenericContainer hallItems;
    private Location hall;
    private Location cellar;
    private Item lamp;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));
        gameContext.setRunIdentity(new GameContext.RunIdentity(PLAYER, ADVENTURE, "The Demo", "1.0.0"));

        pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
        gameContext.setPocket(pocket);
        hallItems = new GenericContainer(new DescriptionProvider("hall items"), 10);
        GenericContainer cellarItems = new GenericContainer(new DescriptionProvider("cellar items"), 10);
        DescriptionProvider hallDescription = new DescriptionProvider("great", "hall");
        hallDescription.setLongDescription("You are in the great hall.");
        hall = new Location(hallDescription, hallItems);
        cellar = new Location(new DescriptionProvider("dark", "cellar"), cellarItems);
        gameContext.setCurrentLocation(hall);
        lamp = new Item(new DescriptionProvider("brass", "lamp"), true);
        hallItems.add(lamp);

        Map<String, Item> items = new HashMap<>();
        items.put(lamp.getId(), lamp);
        Map<String, Container> containers = new HashMap<>();
        for (GenericContainer container : List.of(pocket, hallItems, cellarItems)) {
            containers.put(container.getId(), container);
        }
        Map<String, Location> locations = new HashMap<>();
        locations.put(hall.getId(), hall);
        locations.put(cellar.getId(), cellar);
        AdventureConfig config = mock(AdventureConfig.class);
        when(config.allItems()).thenReturn(items);
        when(config.allContainers()).thenReturn(containers);
        when(config.allLocations()).thenReturn(locations);
        when(config.allVariables()).thenReturn(new VariableProvider());

        GameStateSnapshotter snapshotter = new GameStateSnapshotter(gameContext, config);
        SavedGameService service = new SavedGameService(repositoryOver(store));

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("save", Word.Type.VERB);
        vocabulary.createNewWord("load", Word.Type.VERB);
        vocabulary.createNewWord("lamp", Word.Type.NOUN);
        for (int slot = 1; slot <= 10; slot++) {
            vocabulary.createNewWord(Integer.toString(slot), Word.Type.NOUN);
        }

        Workflow workflow = gameContext.setUpWorkflows();
        GenericCommandDescription save = new GenericCommandDescription("save", "", VocabularyData.WILDCARD_NOUN);
        workflow.addResponse(save, new GenericCommand(save, new SaveGameAction(gameContext, snapshotter, service)));
        GenericCommandDescription load = new GenericCommandDescription("load", "", VocabularyData.WILDCARD_NOUN);
        workflow.addResponse(load, new GenericCommand(load, new LoadGameAction(gameContext, snapshotter, service)));

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    private static SavedGameRepository repositoryOver(Map<String, SavedGameData> store) {
        SavedGameRepository repository = mock(SavedGameRepository.class);
        when(repository.save(any(SavedGameData.class))).thenAnswer(invocation -> {
            SavedGameData data = invocation.getArgument(0);
            store.put(data.getId(), data);
            return data;
        });
        when(repository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.<String>getArgument(0))));
        when(repository.findByUserIdAndAdventureId(anyString(), anyString())).thenAnswer(invocation ->
                store.values().stream()
                     .filter(saved -> saved.getUserId().equals(invocation.getArgument(0))
                                      && saved.getAdventureId().equals(invocation.getArgument(1)))
                     .toList());
        return repository;
    }

    private String id(int slot) {
        return SavedGameData.idFor(PLAYER, ADVENTURE, slot);
    }

    @Test
    void save_withoutNoun_usesTheLowestFreeSlot() {
        gameLoop.processCommand("save");
        gameLoop.processCommand("save");

        assertThat(told.toString()).contains(SystemMessageKey.SM68.defaultText().formatted(1))
                                   .contains(SystemMessageKey.SM68.defaultText().formatted(2));
        assertThat(store).containsOnlyKeys(id(1), id(2));
    }

    @Test
    void save_withASlotNumber_overwritesThatSlot() {
        gameLoop.processCommand("save 3");
        hallItems.remove(lamp);
        pocket.add(lamp);

        gameLoop.processCommand("save 3");

        assertThat(store).containsOnlyKeys(id(3));
        assertThat(store.get(id(3)).getSnapshot().getContainers().get(pocket.getId())).containsExactly(lamp.getId());
    }

    @Test
    void save_whenAllSlotsAreTaken_refusesAndKeepsEverything() {
        for (int slot = 1; slot <= 10; slot++) {
            gameLoop.processCommand("save");
        }
        told.setLength(0);

        gameLoop.processCommand("save");

        assertThat(told.toString()).contains(SystemMessageKey.SM69.defaultText().formatted(10, 10));
        assertThat(store).hasSize(10);
    }

    @Test
    void save_withANounThatIsNoSlotNumber_isRefused() {
        gameLoop.processCommand("save lamp");

        assertThat(told.toString()).contains(SystemMessageKey.SM70.defaultText().formatted(10));
        assertThat(store).isEmpty();
    }

    @Test
    void save_withANumberOutsideTheSlots_isTreatedAsABareSave_becauseTheParserDropsUnknownWords() {
        gameLoop.processCommand("save 11");

        assertThat(store).containsOnlyKeys(id(1));
    }

    @Test
    void load_withoutNoun_listsTheSavedGamesBySlot() {
        gameLoop.processCommand("save");
        gameLoop.processCommand("save");
        told.setLength(0);

        gameLoop.processCommand("load");

        assertThat(told.toString()).contains(SystemMessageKey.SM72.defaultText())
                                   .contains("1. The Demo - ")
                                   .contains("2. The Demo - ");
    }

    @Test
    void load_withoutAnySaves_saysSo() {
        gameLoop.processCommand("load");

        assertThat(told.toString()).contains(SystemMessageKey.SM73.defaultText());
    }

    @Test
    void load_restoresTheSavedState_andDescribesTheLocation() {
        gameLoop.processCommand("save 1");
        hallItems.remove(lamp);
        pocket.add(lamp);
        gameContext.setCurrentLocation(cellar);
        told.setLength(0);

        gameLoop.processCommand("load 1");

        assertThat(hallItems.getContents()).containsExactly(lamp);
        assertThat(pocket.getContents()).isEmpty();
        assertThat(gameContext.getCurrentLocation()).isSameAs(hall);
        assertThat(told.toString()).contains(SystemMessageKey.SM74.defaultText().formatted(1))
                                   .contains("You are in the great hall.");
    }

    @Test
    void load_followedByJustTheNumber_loadsThatSlot() {
        gameLoop.processCommand("save 3");
        hallItems.remove(lamp);
        pocket.add(lamp);

        gameLoop.processCommand("load");
        gameLoop.processCommand("3");

        assertThat(hallItems.getContents()).containsExactly(lamp);
    }

    @Test
    void load_anEmptySlot_isReported() {
        gameLoop.processCommand("load 5");

        assertThat(told.toString()).contains(SystemMessageKey.SM75.defaultText().formatted(5));
    }

    @Test
    void load_whenTheSavedLocationIsGone_saysItCannotBeLoaded_andChangesNothing() {
        SavedGameData saved = new SavedGameData();
        saved.setId(id(2));
        saved.setUserId(PLAYER);
        saved.setAdventureId(ADVENTURE);
        saved.setSlot(2);
        GameSnapshotData snapshot = new GameSnapshotData();
        snapshot.setCurrentLocationId("gone");
        saved.setSnapshot(snapshot);
        store.put(saved.getId(), saved);

        gameLoop.processCommand("load 2");

        assertThat(told.toString()).contains(SystemMessageKey.SM76.defaultText());
        assertThat(gameContext.getCurrentLocation()).isSameAs(hall);
        assertThat(hallItems.getContents()).containsExactly(lamp);
    }

    @Test
    void load_afterTheAdventureChangedVersion_addsAWarning() {
        gameLoop.processCommand("save 1");
        gameContext.setRunIdentity(new GameContext.RunIdentity(PLAYER, ADVENTURE, "The Demo", "2.0.0"));
        told.setLength(0);

        gameLoop.processCommand("load 1");

        assertThat(told.toString()).contains(SystemMessageKey.SM77.defaultText().formatted("1.0.0", "2.0.0"));
    }

    @Test
    void load_whenAVersionIsUnknown_addsNoWarning() {
        gameLoop.processCommand("save 1");
        gameContext.setRunIdentity(new GameContext.RunIdentity(PLAYER, ADVENTURE, "The Demo", null));
        told.setLength(0);

        gameLoop.processCommand("load 1");

        assertThat(told.toString()).doesNotContain("saved with version");
    }

    @Test
    void savesOfAnotherPlayer_areNeitherListedNorLoadable() {
        SavedGameData foreign = new SavedGameData();
        foreign.setId(SavedGameData.idFor("someone-else", ADVENTURE, 1));
        foreign.setUserId("someone-else");
        foreign.setAdventureId(ADVENTURE);
        foreign.setSlot(1);
        foreign.setSnapshot(new GameSnapshotData());
        store.put(foreign.getId(), foreign);

        gameLoop.processCommand("load");
        gameLoop.processCommand("load 1");

        assertThat(told.toString()).contains(SystemMessageKey.SM73.defaultText())
                                   .contains(SystemMessageKey.SM75.defaultText().formatted(1));
    }

    @Test
    void saveAndLoad_withoutARunIdentity_areUnavailable() {
        gameContext.setRunIdentity(null);

        gameLoop.processCommand("save");
        gameLoop.processCommand("load");

        assertThat(told.toString()).contains(SystemMessageKey.SM71.defaultText());
        assertThat(store).isEmpty();
    }
}
