package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class AdventureRunSessionTest {

    private GameContext gameContext;
    private AdventureRunSession session;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setPocket(new GenericContainer(new DescriptionProvider("pocket"), 10));

        DescriptionProvider roomDescription = new DescriptionProvider("throne", "room");
        roomDescription.setLongDescription("A grand throne room.");
        Location room = new Location(roomDescription, new GenericContainer(new DescriptionProvider("room items"), 10));
        gameContext.setCurrentLocation(room);

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("quit", Word.Type.VERB);
        vocabulary.createNewWord("describe", Word.Type.VERB);

        Workflow workflow = gameContext.setUpWorkflows();
        new CommandFactory(new MessagesHolder(), gameContext, new VocabularyData()).setUpWorkflowCommands(workflow);

        session = new AdventureRunSession(new GameLoop(new Parser(vocabulary), gameContext), gameContext);
    }

    @Test
    void submit_filtersOutTheConsolePromptLine() {
        RunResult result = session.submit("describe");

        assertThat(result.lines()).singleElement().asString().contains("A grand throne room.");
    }

    @Test
    void submit_quit_setsGameOver() {
        RunResult result = session.submit("quit");

        assertThat(result.gameOver()).isTrue();
        assertThat(session.isGameOver()).isTrue();
    }

    @Test
    void submit_afterGameOver_doesNothingFurther() {
        session.submit("quit");

        RunResult result = session.submit("describe");

        assertThat(result.lines()).isEmpty();
        assertThat(result.gameOver()).isTrue();
    }

    @Test
    void submit_clearsTheOutputSinkAfterEachCall_soItDoesNotLeakIntoLaterConsoleUse() {
        session.submit("describe");

        StringBuilder afterSessionOutput = new StringBuilder();
        // Nothing installs a sink at this point; tell() should already be back to its default,
        // not still routed into a list this test no longer has access to.
        gameContext.setOutputSink(afterSessionOutput::append);
        gameContext.tell("hello");

        assertThat(afterSessionOutput.toString()).isEqualTo("hello");
    }
}
