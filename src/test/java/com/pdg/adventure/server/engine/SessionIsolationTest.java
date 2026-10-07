package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.support.FakeSessionScope;
import com.pdg.adventure.support.FakeSessionScopeConfig;

/** Two fake browser sessions against the real scoped beans: nothing one session does shows up in the other. */
class SessionIsolationTest {

    private AnnotationConfigApplicationContext context;
    private FakeSessionScope sessions;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(FakeSessionScopeConfig.class, GameContext.class, AdventureConfig.class,
                         MapperSupporter.class);
        context.refresh();
        sessions = context.getBean(FakeSessionScope.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void gameContextFields_areIndependentPerSession() {
        GameContext gameContext = context.getBean(GameContext.class);

        sessions.useSession("anna");
        gameContext.setCurrentNoun("lamp");
        sessions.useSession("ben");
        assertThat(gameContext.getCurrentNoun()).isEqualTo(VocabularyData.EMPTY_STRING);
        gameContext.setCurrentNoun("key");
        sessions.useSession("anna");

        assertThat(gameContext.getCurrentNoun()).isEqualTo("lamp");
    }

    @Test
    void outputSink_ofOneSessionNeverReceivesTheOtherSessionsOutput() {
        GameContext gameContext = context.getBean(GameContext.class);
        List<String> annasOutput = new ArrayList<>();

        sessions.useSession("anna");
        gameContext.setOutputSink(annasOutput::add);
        sessions.useSession("ben");
        gameContext.tell("for ben");

        assertThat(annasOutput).isEmpty();
    }

    @Test
    void variables_areIndependentPerSession() {
        AdventureConfig config = context.getBean(AdventureConfig.class);

        sessions.useSession("anna");
        config.allVariables().set("score", 5);
        sessions.useSession("ben");

        assertThat(config.allVariables().isDefined("score")).isFalse();
    }

    @Test
    void mapperSupportersRegistries_followTheCurrentSession() {
        MapperSupporter supporter = context.getBean(MapperSupporter.class);
        Location annasRoom = mock(Location.class);
        when(annasRoom.getId()).thenReturn("room");

        sessions.useSession("anna");
        supporter.addMappedLocation(annasRoom);
        sessions.useSession("ben");
        assertThat(supporter.getMappedLocation("room")).isNull();
        sessions.useSession("anna");

        assertThat(supporter.getMappedLocation("room")).isSameAs(annasRoom);
    }
}
