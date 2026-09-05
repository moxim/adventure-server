package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
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

    private GameContext gameContext;
    private AdventureRunSessionFactory factory;
    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        lenient().when(adventureConfig.allMessages()).thenReturn(new MessagesHolder());
        lenient().when(adventureConfig.allLocations()).thenReturn(new HashMap<>());
        lenient().when(adventureConfig.allItems()).thenReturn(new HashMap<>());
        lenient().when(adventureConfig.allContainers()).thenReturn(new HashMap<>());
        vocabulary = new Vocabulary();
        lenient().when(adventureConfig.allWords()).thenReturn(vocabulary);
        factory = new AdventureRunSessionFactory(adventureService, adventureMapper, workflowMapper, adventureConfig,
                                                  gameContext);
    }

    @Test
    void start_successfulLoad_returnsASessionThatCanPlayTheOpeningRoom() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");

        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        when(startLocation.getLongDescription()).thenReturn("A grand throne room.");
        // Precompute the stubbed return value before opening when(...): calling a mock (getId()
        // etc., inside adventureBoundTo) while a when(...) stubbing is still "armed" waiting for
        // its thenReturn() throws UnfinishedStubbingException.
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        AdventureRunSession session = factory.start(adventureData);
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

        AdventureRunSession session = factory.start(adventureData);
        RunResult result = session.submit("inventory");

        assertThat(result.lines()).contains(SystemMessageKey.SM9.defaultText());
    }

    @Test
    void start_compoundCommand_runsBothSubCommandsThroughTheRealSeededVocabulary() {
        // "and" must be seeded by the real registerBaseVerbs() production path, not just by a
        // hand-built test Vocabulary - this is the browser/"Run Adventure" entry point.
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        when(startLocation.getLongDescription()).thenReturn("A grand throne room.");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        AdventureRunSession session = factory.start(adventureData);
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

        factory.start(adventureData);

        assertThat(vocabulary.getType("it")).isEqualTo(Word.Type.PRONOUN);
    }

    @Test
    void start_adventureNotFound_throwsIllegalStateException() {
        AdventureData adventureData = new AdventureData();
        adventureData.setId("missing-adv");

        when(adventureService.findAdventureById("missing-adv")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factory.start(adventureData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing-adv");
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
