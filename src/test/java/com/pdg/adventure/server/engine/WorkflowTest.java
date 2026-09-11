package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

class WorkflowTest {

    private final List<String> told = new ArrayList<>();
    private Workflow workflow;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(told::add);
        workflow = gameContext.setUpWorkflows();
    }

    @Test
    void runProcesses_executesProcessesInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
        addProcess(new GenericCommandDescription("zoo"), "Zoo message.");
        addProcess(new GenericCommandDescription("apple"), "Apple message.");
        addProcess(new GenericCommandDescription("middle"), "Middle message.");

        workflow.runProcesses();

        assertThat(told).containsExactly("Apple message.", "Middle message.", "Zoo message.");
    }

    // CommandFactory.setUpWorkflowCommands registers a sentinel Process keyed ("~", "~", "~")
    // whose action prints the turn prompt ("What now? > "; SM2) - it must keep coming after every
    // author-authored ambient message each turn, the way the old TreeMap iteration (sorted by the
    // "verb|adjective|noun" description string, where '|' and '~' both sort above lowercase
    // letters) already guaranteed. String.CASE_INSENSITIVE_ORDER must preserve that.
    @Test
    void runProcesses_stillOrdersTheTurnPromptSentinelLast() {
        addProcess(new GenericCommandDescription("zebra"), "Zebra message.");
        addProcess(new GenericCommandDescription("apple"), "Apple message.");
        addProcess(new GenericCommandDescription("~", "~", "~"), SystemMessageKey.SM2.defaultText());

        workflow.runProcesses();

        assertThat(told).containsExactly("Apple message.", "Zebra message.", SystemMessageKey.SM2.defaultText());
    }

    @Test
    void runProcesses_ordersSameVerbProcessesByAdjectiveThenNoun() {
        addProcess(new GenericCommandDescription("look", "big", "door"), "Big door.");
        addProcess(new GenericCommandDescription("look", "big", "chest"), "Big chest.");
        addProcess(new GenericCommandDescription("look", "small", "box"), "Small box.");

        workflow.runProcesses();

        assertThat(told).containsExactly("Big chest.", "Big door.", "Small box.");
    }

    @Test
    void runArrivalProcesses_executesArrivalProcessesInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
        addArrivalProcess(new GenericCommandDescription("zoo"), "Zoo arrival message.");
        addArrivalProcess(new GenericCommandDescription("apple"), "Apple arrival message.");

        workflow.runArrivalProcesses();

        assertThat(told).containsExactly("Apple arrival message.", "Zoo arrival message.");
    }

    @Test
    void runArrivalProcesses_doesNotExecuteRegularProcesses() {
        addProcess(new GenericCommandDescription("regular"), "Regular process message.");
        addArrivalProcess(new GenericCommandDescription("arrival"), "Arrival process message.");

        workflow.runArrivalProcesses();

        assertThat(told).containsExactly("Arrival process message.");
    }

    @Test
    void runProcesses_doesNotExecuteArrivalProcesses() {
        addProcess(new GenericCommandDescription("regular"), "Regular process message.");
        addArrivalProcess(new GenericCommandDescription("arrival"), "Arrival process message.");

        workflow.runProcesses();

        assertThat(told).containsExactly("Regular process message.");
    }

    private void addProcess(GenericCommandDescription aDescription, String aMessage) {
        Command command = mock(Command.class);
        when(command.execute()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS, aMessage));
        workflow.addProcess(aDescription, command);
    }

    private void addArrivalProcess(GenericCommandDescription aDescription, String aMessage) {
        Command command = mock(Command.class);
        when(command.execute()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS, aMessage));
        workflow.addArrivalProcess(aDescription, command);
    }
}
