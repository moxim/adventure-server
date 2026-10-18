package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.condition.ClickedCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.condition.PrepositionCondition;
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/** A click on the picture through the real Workflow and GameLoop: Processes only, hit or miss, SM6, turns. */
class ClickGameLoopTest {

    private final List<String> told = new ArrayList<>();
    private final VariableProvider variables = new VariableProvider();
    private GameContext gameContext;
    private Workflow workflow;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        // As AdventureRunSession does: blank lines are not shown.
        gameContext.setOutputSink(line -> {
            if (line != null && !line.isBlank()) {
                told.add(line);
            }
        });
        gameContext.setPocket(new GenericContainer(new DescriptionProvider("pocket"), 10));
        gameContext.setCurrentLocation(new Location(new DescriptionProvider("throne", "room"),
                                                    new GenericContainer(new DescriptionProvider("room items"), 10)));
        workflow = gameContext.setUpWorkflows();
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 0);
        gameLoop = new GameLoop(new Parser(new Vocabulary()), gameContext, variables);
    }

    private int turns() {
        return variables.get(VariableProvider.TURNS_VARIABLE_NAME).orElseThrow().value();
    }

    /** A Process (empty verb) with the given preconditions, telling the message when it applies. */
    private void process(String aMessage, PreCondition... somePreconditions) {
        process(new MessageAction(aMessage), somePreconditions);
    }

    private void process(Action anAction, PreCondition... somePreconditions) {
        GenericCommandDescription description = new GenericCommandDescription(VocabularyData.EMPTY_STRING);
        GenericCommand command = new GenericCommand(description, anAction);
        for (PreCondition precondition : somePreconditions) {
            command.addPreCondition(precondition);
        }
        workflow.addProcess(description, command);
    }

    private ClickedCondition clicked(int x, int y) {
        return new ClickedCondition(x, y, gameContext);
    }

    @Test
    void click_onTheCell_runsTheActionsAndCountsATurn() {
        process("The door opens.", clicked(3, 4));

        GameLoop.CommandOutcome outcome = gameLoop.processClick(3, 4);

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told).containsExactly("The door opens.");
        assertThat(turns()).isEqualTo(1);
    }

    @Test
    void click_onTheCell_whenAnActionFails_isStillAHit_andTellsTheFailureInsteadOfSm6() {
        Action failing = mock(Action.class);
        when(failing.execute()).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "The door is stuck."));
        process(failing, clicked(3, 4));

        gameLoop.processClick(3, 4);

        assertThat(told).containsExactly("The door is stuck.");
        assertThat(turns()).isEqualTo(1);
    }

    @Test
    void click_onAnotherCell_tellsSm6AndDoesNotCountATurn() {
        process("The door opens.", clicked(3, 4));

        gameLoop.processClick(5, 5);

        assertThat(told).containsExactly(SystemMessageKey.SM6.defaultText());
        assertThat(turns()).isZero();
    }

    @Test
    void click_whenTheClickedProcessHasAFailingOtherGuard_isAMiss() {
        PreCondition failing = mock(PreCondition.class);
        when(failing.check()).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE));
        process("The door opens.", clicked(3, 4), failing);

        gameLoop.processClick(3, 4);

        assertThat(told).containsExactly(SystemMessageKey.SM6.defaultText());
        assertThat(turns()).isZero();
    }

    @Test
    void click_withOnlyANotClickedProcessMatching_isAMiss() {
        process("Not there.", new NotCondition(clicked(0, 0)));

        gameLoop.processClick(3, 4);

        assertThat(told).containsExactly(SystemMessageKey.SM6.defaultText(), "Not there.");
        assertThat(turns()).isZero();
    }

    @Test
    void click_alsoRunsProcessesWithoutClicked_andTheirMessageFollowsSm6OnAMiss() {
        process("Time passes.");

        gameLoop.processClick(1, 1);

        assertThat(told).containsExactly(SystemMessageKey.SM6.defaultText(), "Time passes.");
    }

    @Test
    void click_onAHit_tellsTheProcessMessagesAndNoSm6() {
        process("Time passes.");
        process("The door opens.", clicked(3, 4));

        gameLoop.processClick(3, 4);

        assertThat(told).doesNotContain(SystemMessageKey.SM6.defaultText());
        assertThat(String.join(" ", told)).contains("Time passes.").contains("The door opens.");
    }

    @Test
    void click_doesNotSeeTheLastTypedCommand() {
        gameContext.setCurrentPreposition("on");
        process("Switched on.", new PrepositionCondition("on", gameContext));

        gameLoop.processClick(3, 4);

        assertThat(told).containsExactly(SystemMessageKey.SM6.defaultText());
    }

    @Test
    void click_endsWithTheClickCleared_afterHitAndAfterMiss() {
        process("The door opens.", clicked(3, 4));

        gameLoop.processClick(3, 4);
        assertThat(gameContext.isClickAt(3, 4)).isFalse();
        assertThat(gameContext.isClickHandled()).isFalse();

        gameLoop.processClick(5, 5);
        assertThat(gameContext.isClickAt(5, 5)).isFalse();
    }

    @Test
    void typedTurnAfterAClick_neverSeesClickedAsTrue() {
        process("The door opens.", clicked(3, 4));
        gameLoop.processClick(3, 4);

        assertThat(clicked(3, 4).check().getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        told.clear();
        gameLoop.processCommand("anything");
        assertThat(told).doesNotContain("The door opens.");
    }

    @Test
    void click_whenAProcessQuits_returnsQuitAndClearsTheClick() {
        process(new QuitAction(), clicked(3, 4));

        GameLoop.CommandOutcome outcome = gameLoop.processClick(3, 4);

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.QUIT);
        assertThat(gameContext.isClickAt(3, 4)).isFalse();
    }

    @Test
    void click_whenAProcessThrowsUnresolvedReference_tellsItAndContinues() {
        Action broken = mock(Action.class);
        when(broken.execute()).thenThrow(new UnresolvedReferenceException("No such place."));
        process(broken, clicked(3, 4));

        GameLoop.CommandOutcome outcome = gameLoop.processClick(3, 4);

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told).contains("No such place.");
        assertThat(gameContext.isClickAt(3, 4)).isFalse();
    }

    @Test
    void click_onTheDoor_movesThePlayer_andAMissLeavesThemWhereTheyAre() {
        Location start = gameContext.getCurrentLocation();
        DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "room");
        cellarDescription.setLongDescription("A damp cellar.");
        Location cellar = new Location(cellarDescription, new GenericContainer(new DescriptionProvider("cellar items"), 10));
        process(new MovePlayerAction(cellar, gameContext, variables), clicked(3, 4));

        gameLoop.processClick(5, 5);
        assertThat(gameContext.getCurrentLocation()).isSameAs(start);
        assertThat(told).containsExactly(SystemMessageKey.SM6.defaultText());

        told.clear();
        gameLoop.processClick(3, 4);
        assertThat(gameContext.getCurrentLocation()).isSameAs(cellar);
        assertThat(String.join(" ", told)).contains("A damp cellar.").doesNotContain(SystemMessageKey.SM6.defaultText());
        assertThat(turns()).isEqualTo(1);
    }
}
