package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * Exercises processCommand(String) directly, i.e. the browser Test session's entry point into
 * the engine (no BufferedReader involved). Wires the same interceptor commands
 * AdventureRunSessionFactory registers in production, via the real CommandFactory.
 */
class GameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        gameContext.setPocket(new GenericContainer(new DescriptionProvider("pocket"), 10));

        DescriptionProvider roomDescription = new DescriptionProvider("throne", "room");
        roomDescription.setLongDescription("A grand throne room.");
        Location room = new Location(roomDescription, new GenericContainer(new DescriptionProvider("room items"), 10));
        gameContext.setCurrentLocation(room);

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("quit", Word.Type.VERB);
        vocabulary.createNewWord("describe", Word.Type.VERB);
        vocabulary.createNewWord("take", Word.Type.VERB); // recognised, but wired to nothing
        vocabulary.createNewWord("suit", Word.Type.NOUN); // recognised noun, no verb given in some tests

        Workflow workflow = gameContext.setUpWorkflows();
        new CommandFactory(new MessagesHolder(), gameContext, new VocabularyData()).setUpWorkflowCommands(workflow);

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    @Test
    void describe_tellsTheCurrentLocationsLongDescription() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A grand throne room.");
    }

    @Test
    void quit_tellsGoodbye_andReturnsQuit() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("quit");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.QUIT);
        assertThat(told.toString()).contains("Bye bye.");
    }

    @Test
    void unrecognisedInput_tellsPleaseRephrase_andContinues() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("mumble grumble");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't understand, please rephrase.");
    }

    @Test
    void recognisedVerbWithNoMatchingCommand_tellsItDoesNotKnowHow() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't know how to do that.");
    }

    @Test
    void bareNounWithNoVerb_tellsPleaseRephrase_notIDontKnowHow() {
        // "suit" is a recognised noun but no verb was given, so this must be treated the
        // same as unparseable input, not as a command that merely fails to find a match.
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("suit");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't understand, please rephrase.");
        assertThat(told.toString()).doesNotContain("I don't know how to do that.");
    }
}
