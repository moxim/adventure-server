package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
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
    private GameContext gameContext;
    private Vocabulary vocabulary;
    private Workflow workflow;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        gameContext.setPocket(new GenericContainer(new DescriptionProvider("pocket"), 10));

        DescriptionProvider roomDescription = new DescriptionProvider("throne", "room");
        roomDescription.setLongDescription("A grand throne room.");
        Location room = new Location(roomDescription, new GenericContainer(new DescriptionProvider("room items"), 10));
        gameContext.setCurrentLocation(room);

        vocabulary = new Vocabulary();
        vocabulary.createNewWord("quit", Word.Type.VERB);
        vocabulary.createNewWord("describe", Word.Type.VERB);
        vocabulary.createNewWord("take", Word.Type.VERB); // recognised, but wired to nothing
        vocabulary.createNewWord("suit", Word.Type.NOUN); // recognised noun, no verb given in some tests
        vocabulary.createNewWord("help", Word.Type.VERB);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        vocabulary.createSynonym("then", "and");

        workflow = gameContext.setUpWorkflows();
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

    @Test
    void and_runsBothSubCommandsInOrder() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe and help");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        String output = told.toString();
        assertThat(output).contains("A grand throne room.").contains("Look around, examine items");
        assertThat(output.indexOf("A grand throne room."))
                .isLessThan(output.indexOf("Look around, examine items"));
    }

    @Test
    void period_behavesTheSameAsAnd() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe. help");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A grand throne room.").contains("Look around, examine items");
    }

    @Test
    void and_stopsAtFirstFailure_secondSubCommandNeverRuns() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take and describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't know how to do that.");
        assertThat(told.toString()).doesNotContain("A grand throne room.");
    }

    @Test
    void and_bareNounFirstSubCommand_stopsSequence() {
        // "suit" alone is a recognised noun with no verb - unparseable on its own (see
        // bareNounWithNoVerb_tellsPleaseRephrase_notIDontKnowHow above) - and must stop the
        // sequence the same way a real command failure would, not let "describe" run anyway.
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("suit and describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't understand, please rephrase.");
        assertThat(told.toString()).doesNotContain("A grand throne room.");
    }

    @Test
    void and_quitMidSequence_returnsQuit_laterSubCommandsNeverRun() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe and quit and help");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.QUIT);
        assertThat(told.toString()).contains("A grand throne room.").contains("Bye bye.");
        assertThat(told.toString()).doesNotContain("Look around, examine items");
    }

    @Test
    void and_afterMovingSubCommand_secondSubCommandSeesTheNewLocation() {
        // A command wired only on the destination location ("examine key") must be reachable
        // by the second sub-command of a sequence whose first sub-command moved the player
        // there - proves GameLoop re-reads the current location per sub-command rather than
        // capturing it once for the whole turn.
        MessagesHolder messages = new MessagesHolder();
        DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
        cellarDescription.setLongDescription("A dark, damp cellar.");
        Location cellar = new Location(cellarDescription,
                                       new GenericContainer(new DescriptionProvider("cellar items"), 10));
        cellar.addCommand(new GenericCommand(new GenericCommandDescription("examine", "key"),
                                             new MessageAction("A rusty key.", messages)));

        GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
        workflow.addInterceptorCommand(descendDescription,
                                       new GenericCommand(descendDescription,
                                                          new MovePlayerAction(cellar, messages, gameContext)));

        vocabulary.createNewWord("descend", Word.Type.VERB);
        vocabulary.createNewWord("examine", Word.Type.VERB);
        vocabulary.createNewWord("key", Word.Type.NOUN);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("descend and examine key");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A dark, damp cellar.").contains("A rusty key.");
        assertThat(told.toString()).doesNotContain("I don't know how to do that.");
    }
}
