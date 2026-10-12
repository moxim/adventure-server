package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.TurnsAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * The TURNS variable and the TURNS action through the real parser and game loop, as PAW specifies them: every valid
 * phrase the parser extracts is a turn (its flags 31/32), and the TURNS action prints "You have taken x turn(s)."
 */
class TurnsGameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private final VariableProvider variables = new VariableProvider();
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));
        gameContext.setPocket(new GenericContainer(new DescriptionProvider("pocket"), 10));
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("throne", "room"),
                                                    new GenericContainer(new DescriptionProvider("room items"), 10)));

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("turns", Word.Type.VERB);
        vocabulary.createNewWord("wait", Word.Type.VERB);
        vocabulary.createNewWord("take", Word.Type.VERB); // recognised, but wired to nothing
        vocabulary.createNewWord("suit", Word.Type.NOUN); // a noun without a verb is not a phrase
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);

        Workflow workflow = gameContext.setUpWorkflows();
        add(workflow, "turns", new TurnsAction(gameContext::tell, variables));
        add(workflow, "wait", new MessageAction("Time passes."));

        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 0); // as a run starts
        gameLoop = new GameLoop(new Parser(vocabulary), gameContext, variables);
    }

    private static void add(Workflow aWorkflow, String aVerb, com.pdg.adventure.api.Action anAction) {
        GenericCommandDescription description = new GenericCommandDescription(aVerb);
        aWorkflow.addResponse(description, new GenericCommand(description, anAction));
    }

    @Test
    void turns_asTheFirstCommand_countsItselfAsTheFirstTurnInTheSingularForm() {
        gameLoop.processCommand("turns");

        assertThat(told.toString()).contains("You have taken 1 turn.").doesNotContain("turns.");
    }

    @Test
    void everyUnderstoodCommandIsOneTurn() {
        gameLoop.processCommand("wait");
        gameLoop.processCommand("wait");
        gameLoop.processCommand("turns");

        assertThat(told.toString()).contains("You have taken 3 turns.");
        assertThat(variables.get(VariableProvider.TURNS_VARIABLE_NAME).map(Variable::value)).contains(3);
    }

    @Test
    void aPhraseTheEngineCouldNotUseAsACommandIsNotATurn() {
        gameLoop.processCommand("suit"); // a noun but no verb: "I was not able to understand any of that"
        gameLoop.processCommand("turns");

        assertThat(told.toString()).contains("You have taken 1 turn.");
    }

    @Test
    void eachPhraseOfAConjunctionIsATurn() {
        gameLoop.processCommand("wait and wait");
        gameLoop.processCommand("turns");

        assertThat(told.toString()).contains("You have taken 3 turns.");
    }

    @Test
    void aPhraseThatFailsIsStillATurn_andItsRemainingPhrasesAreNotCounted() {
        gameLoop.processCommand("take and wait"); // "take" does nothing, so "wait" is never attempted
        gameLoop.processCommand("turns");

        assertThat(told.toString()).contains("You have taken 2 turns.");
    }

    @Test
    void theLoopCountsFromZeroEvenIfNothingSeededTheVariable() {
        variables.clear();

        gameLoop.processCommand("turns");

        assertThat(told.toString()).contains("You have taken 1 turn.");
    }
}
