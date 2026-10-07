package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.mapper.WorkflowMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@ExtendWith(MockitoExtension.class)
class AdventureRunSessionFactoryTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private AdventureMapper adventureMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private Location startLocation;

    private final Map<String, Item> items = new HashMap<>();
    private GameContext gameContext;
    private ActiveRun activeRun;
    private AdventureRunSessionFactory factory;
    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        lenient().when(adventureConfig.allMessages()).thenReturn(new MessagesHolder());
        lenient().when(adventureConfig.allLocations()).thenReturn(new HashMap<>());
        lenient().when(adventureConfig.allItems()).thenReturn(items);
        lenient().when(adventureConfig.allVariables()).thenReturn(new VariableProvider());
        lenient().when(adventureConfig.allContainers()).thenReturn(new HashMap<>());
        vocabulary = new Vocabulary();
        lenient().when(adventureConfig.allWords()).thenReturn(vocabulary);
        activeRun = new ActiveRun();
        factory = new AdventureRunSessionFactory(adventureService, adventureMapper, workflowMapper, adventureConfig,
                                                  gameContext, activeRun);
    }

    @Test
    void start_successfulLoad_returnsASessionThatCanPlayTheOpeningRoom() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");

        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        // "look" resolves through getLookDescription() (see CommandFactory), not
        // getLongDescription() - stub it so the mocked Location doesn't return null.
        when(startLocation.getLookDescription())
                .thenReturn(new Location.LocationDescription("A grand throne room.", null));
        // Precompute the stubbed return value before opening when(...): calling a mock (getId()
        // etc., inside adventureBoundTo) while a when(...) stubbing is still "armed" waiting for
        // its thenReturn() throws UnfinishedStubbingException.
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        AdventureRunSession session = factory.start(adventureData, new RunOwner("player-1"));
        RunResult result = session.submit("look");

        assertThat(result.gameOver()).isFalse();
        assertThat(result.lines()).singleElement().asString().contains("A grand throne room.");
    }

    @Test
    void start_thenInventory_includesTheGenericCarryHeaderMessage() {
        // LoadAdventureAction clears allMessages and repopulates only from the adventure's own
        // persisted messages - without also re-seeding this generic, adventure-independent id,
        // the header silently disappears (WearAction/RemoveAction/CreateAction/MoveItemAction/
        // DestroyAction go further and throw NPE, since they call .formatted() on the result).
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        AdventureRunSession session = factory.start(adventureData, new RunOwner("player-1"));
        RunResult result = session.submit("inventory");

        assertThat(result.lines()).contains(SystemMessageKey.SM9.defaultText());
    }

    @Test
    void start_lookThenUnrelatedCommand_picturePersistsAcrossTheUnrelatedCommand() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        when(startLocation.getLookDescription())
                .thenReturn(new Location.LocationDescription("A grand throne room.", "pic-1"));
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        AdventureRunSession session = factory.start(adventureData, new RunOwner("player-1"));
        session.submit("look");
        assertThat(gameContext.getCurrentPictureId()).isEqualTo("pic-1");

        session.submit("inventory");
        assertThat(gameContext.getCurrentPictureId()).isEqualTo("pic-1");
    }

    @Test
    void start_compoundCommand_runsBothSubCommandsThroughTheRealSeededVocabulary() {
        // "and" must be seeded by the real registerBaseVerbs() production path, not just by a
        // hand-built test Vocabulary - this is the browser/"Run Adventure" entry point.
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        // "describe" resolves through getLookDescription() (see CommandFactory), not
        // getLongDescription() - stub it so the mocked Location doesn't return null.
        when(startLocation.getLookDescription())
                .thenReturn(new Location.LocationDescription("A grand throne room.", null));
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        AdventureRunSession session = factory.start(adventureData, new RunOwner("player-1"));
        RunResult result = session.submit("describe and inventory");

        assertThat(result.lines()).anySatisfy(line -> assertThat(line).contains("A grand throne room."));
        assertThat(result.lines()).anySatisfy(line -> assertThat(line).contains(SystemMessageKey.SM9.defaultText()));
    }

    @Test
    void start_seedsPronounIt_throughTheRealRegisterBaseVerbsPath() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        factory.start(adventureData, new RunOwner("player-1"));

        assertThat(vocabulary.getType("it")).isEqualTo(Word.Type.PRONOUN);
    }

    @Test
    void start_adventureNotFound_throwsIllegalStateException() {
        AdventureData adventureData = new AdventureData();
        adventureData.setId("missing-adv");

        when(adventureService.findAdventureById("missing-adv")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factory.start(adventureData, new RunOwner("player-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing-adv");
    }

    private AdventureData startableAdventure() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);
        return adventureData;
    }

    @Test
    void start_whileAnotherRunIsActive_throwsAndNeverTouchesTheEngineAgain() {
        AdventureData adventureData = startableAdventure();
        factory.start(adventureData, new RunOwner("player-1"));

        assertThatThrownBy(() -> factory.start(adventureData, new RunOwner("player-1")))
                .isInstanceOf(RunAlreadyActiveException.class);

        verify(adventureMapper, times(1)).mapToBO(adventureData);
    }

    @Test
    void start_afterTheOwnerIsGone_succeeds() {
        AdventureData adventureData = startableAdventure();
        RunOwner first = new RunOwner("player-1");
        factory.start(adventureData, first);
        first.markGone();

        AdventureRunSession second = factory.start(adventureData, new RunOwner("player-1"));

        assertThat(second.isGameOver()).isFalse();
        verify(adventureMapper, times(2)).mapToBO(adventureData);
    }

    @Test
    void start_afterTheRunEnded_succeeds() {
        AdventureData adventureData = startableAdventure();
        AdventureRunSession first = factory.start(adventureData, new RunOwner("player-1"));
        first.submit("quit");

        AdventureRunSession second = factory.start(adventureData, new RunOwner("player-1"));

        assertThat(second.isGameOver()).isFalse();
    }

    @Test
    void startReplacingActive_supersedesTheOldSession() {
        AdventureData adventureData = startableAdventure();
        AdventureRunSession old = factory.start(adventureData, new RunOwner("player-1"));

        AdventureRunSession replacement = factory.startReplacingActive(adventureData, new RunOwner("player-1"));
        RunResult oldResult = old.submit("look");

        assertThat(oldResult.gameOver()).isTrue();
        assertThat(oldResult.lines()).containsExactly(AdventureRunSession.ENDED_ELSEWHERE_TEXT);
        assertThat(replacement.isGameOver()).isFalse();
    }

    @Test
    void release_byTheCurrentOwner_clearsTheRegistriesAndTheContext_andAllowsANewStart() {
        AdventureData adventureData = startableAdventure();
        RunOwner owner = new RunOwner("player-1");
        factory.start(adventureData, owner);
        items.put("sword", mock(Item.class));
        gameContext.setCurrentPictureId("pic-1");

        factory.release(owner);

        assertThat(items).isEmpty();
        assertThat(gameContext.getCurrentLocation()).isNull();
        assertThat(gameContext.getCurrentPictureId()).isNull();
        assertThat(factory.start(adventureData, new RunOwner("player-1")).isGameOver()).isFalse();
    }

    @Test
    void release_byAStaleOwner_keepsTheNewerRunsRegistries() {
        AdventureData adventureData = startableAdventure();
        RunOwner stale = new RunOwner("player-1");
        factory.start(adventureData, stale);
        stale.markGone();
        factory.start(adventureData, new RunOwner("player-1"));
        items.put("sword", mock(Item.class));

        factory.release(stale);

        assertThat(items).containsKey("sword");
        assertThatThrownBy(() -> factory.start(adventureData, new RunOwner("player-1")))
                .isInstanceOf(RunAlreadyActiveException.class);
    }

    @Test
    void start_bindsTheAdventuresSystemMessageOverridesIntoTheSession() {
        AdventureData adventureData = startableAdventure();
        adventureData.getSystemMessages().put("9", new SystemMessageData("9", "Carrying:"));

        AdventureRunSession session = factory.start(adventureData, new RunOwner("player-1"));

        assertThat(session.runBound(SystemMessageKey.SM9::defaultText)).isEqualTo("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isNotEqualTo("Carrying:");
    }

    @Test
    void start_hasTheOverridesBoundWhileTheAdventureIsMapped() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        adventureData.getSystemMessages().put("9", new SystemMessageData("9", "Carrying:"));
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        List<String> seenWhileMapping = new ArrayList<>();
        when(adventureMapper.mapToBO(adventureData)).thenAnswer(invocation -> {
            seenWhileMapping.add(SystemMessageKey.SM9.defaultText());
            return adventure;
        });

        factory.start(adventureData, new RunOwner("player-1"));

        assertThat(seenWhileMapping).containsExactly("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isNotEqualTo("Carrying:");
    }

    @Test
    void start_putsTheRunIdentityOnTheContext_andReleaseClearsIt() {
        AdventureData adventureData = startableAdventure();
        adventureData.setTitle("The Demo");
        adventureData.setBuilderVersion("1.2.3");
        RunOwner owner = new RunOwner("player-7");

        factory.start(adventureData, owner);

        assertThat(gameContext.getRunIdentity())
                .isEqualTo(new GameContext.RunIdentity("player-7", "adv-1", "The Demo", "1.2.3"));

        factory.release(owner);

        assertThat(gameContext.getRunIdentity()).isNull();
    }

    @Test
    void start_registersTheSlotNumbersOneToTenAsNouns() {
        AdventureData adventureData = startableAdventure();

        factory.start(adventureData, new RunOwner("player-1"));

        for (int slot = 1; slot <= 10; slot++) {
            assertThat(vocabulary.getType(Integer.toString(slot))).isEqualTo(Word.Type.NOUN);
        }
    }

    private static AdventureData adventureWithOneLocation(String anAdventureId, String aLocationId) {
        AdventureData adventureData = new AdventureData();
        adventureData.setId(anAdventureId);
        adventureData.setCurrentLocationId(aLocationId);
        LocationData locationData = new LocationData();
        locationData.setId(aLocationId);
        adventureData.getLocationData().put(aLocationId, locationData);
        adventureData.setVocabularyData(new VocabularyData());
        return adventureData;
    }

    private static Adventure adventureBoundTo(Location aStartLocation, String aStartLocationId) {
        Adventure adventure = new Adventure(null, null, null, null);
        adventure.setCurrentLocationId(aStartLocationId);
        adventure.setLocations(List.of(aStartLocation));
        return adventure;
    }
}
