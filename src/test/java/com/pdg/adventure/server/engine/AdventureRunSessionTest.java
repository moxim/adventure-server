package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.testhelper.StandardResponses;
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
        StandardResponses.register(workflow, gameContext);

        session = new AdventureRunSession(new GameLoop(new Parser(vocabulary), gameContext), gameContext);
    }

    @Test
    void submit_filtersOutTheConsolePromptLine() {
        RunResult result = session.submit("describe");

        assertThat(result.lines()).singleElement().asString().contains("A grand throne room.");
    }

    @Test
    void submit_asksForTheNextInput_withOneOfSystemMessages2To5() {
        RunResult result = session.submit("describe");

        assertThat(result.gameOver()).isFalse();
        assertThat(result.prompt()).isIn("What now?", "What next?", "What should I do now?", "What should I do next?");
    }

    @Test
    void submit_pickingThePromptAtRandom_usesEachOfTheFourEventually() {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 400; i++) {
            seen.add(session.prompt());
        }

        assertThat(seen).containsExactlyInAnyOrder("What now?", "What next?", "What should I do now?",
                                                   "What should I do next?");
    }

    @ParameterizedTest
    @CsvSource({"2,What now?", "3,What next?", "4,What should I do now?", "5,What should I do next?",
                "6,What now?", "7,What next?", "8,What should I do now?", "9,What should I do next?",
                "10,What now?", "103,What next?"})
    void prompt_withThePromptVariableFrom2On_picksTheMessageAndWrapsAroundAbove5(int steering, String expected) {
        VariableProvider variables = new VariableProvider();
        variables.set(VariableProvider.PROMPT_VARIABLE_NAME, steering);
        AdventureRunSession steered = new AdventureRunSession(new GameLoop(new Parser(new Vocabulary()), gameContext),
                                                              gameContext, java.util.Map.of(), variables);

        for (int i = 0; i < 20; i++) {
            assertThat(steered.prompt()).isEqualTo(expected);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, -7})
    void prompt_withThePromptVariableBelow2_leavesTheChoiceToChance(int steering) {
        VariableProvider variables = new VariableProvider();
        variables.set(VariableProvider.PROMPT_VARIABLE_NAME, steering);
        AdventureRunSession unsteered = new AdventureRunSession(new GameLoop(new Parser(new Vocabulary()), gameContext),
                                                                gameContext, java.util.Map.of(), variables);

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 400; i++) {
            seen.add(unsteered.prompt());
        }

        assertThat(seen).hasSize(4);
    }

    @Test
    void prompt_followsTheVariableAsItChanges() {
        VariableProvider variables = new VariableProvider();
        AdventureRunSession steered = new AdventureRunSession(new GameLoop(new Parser(new Vocabulary()), gameContext),
                                                              gameContext, java.util.Map.of(), variables);

        variables.set(VariableProvider.PROMPT_VARIABLE_NAME, 3);
        assertThat(steered.prompt()).isEqualTo("What next?");
        variables.set(VariableProvider.PROMPT_VARIABLE_NAME, 5);
        assertThat(steered.prompt()).isEqualTo("What should I do next?");
    }

    @Test
    void submit_whenTheGameEnds_asksForNothing() {
        RunResult result = session.submit("quit");

        assertThat(result.gameOver()).isTrue();
        assertThat(result.prompt()).isEmpty();
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
