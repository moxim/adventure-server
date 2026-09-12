package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

class WorkflowTest {

    private Workflow workflow;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        workflow = gameContext.setUpWorkflows();
    }

    @Test
    void runProcesses_executesProcessesInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
        addProcess(new GenericCommandDescription("zoo"), "Zoo message.");
        addProcess(new GenericCommandDescription("apple"), "Apple message.");
        addProcess(new GenericCommandDescription("middle"), "Middle message.");

        String result = workflow.runProcesses().getResultMessage();

        assertThat(result).contains("Apple message.", "Middle message.", "Zoo message.");
        assertThat(result.indexOf("Apple message.")).isLessThan(result.indexOf("Middle message."));
        assertThat(result.indexOf("Middle message.")).isLessThan(result.indexOf("Zoo message."));
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

        String result = workflow.runProcesses().getResultMessage();

        assertThat(result).contains("Apple message.", "Zebra message.", SystemMessageKey.SM2.defaultText());
        assertThat(result.indexOf("Apple message.")).isLessThan(result.indexOf("Zebra message."));
        assertThat(result.indexOf("Zebra message.")).isLessThan(result.indexOf(SystemMessageKey.SM2.defaultText()));
    }

    @Test
    void runProcesses_ordersSameVerbProcessesByAdjectiveThenNoun() {
        addProcess(new GenericCommandDescription("look", "big", "door"), "Big door.");
        addProcess(new GenericCommandDescription("look", "big", "chest"), "Big chest.");
        addProcess(new GenericCommandDescription("look", "small", "box"), "Small box.");

        String result = workflow.runProcesses().getResultMessage();

        assertThat(result).contains("Big chest.", "Big door.", "Small box.");
        assertThat(result.indexOf("Big chest.")).isLessThan(result.indexOf("Big door."));
        assertThat(result.indexOf("Big door.")).isLessThan(result.indexOf("Small box."));
    }

    @Test
    void runArrivalProcesses_executesArrivalProcessesInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
        addArrivalProcess(new GenericCommandDescription("zoo"), "Zoo arrival message.");
        addArrivalProcess(new GenericCommandDescription("apple"), "Apple arrival message.");

        String result = workflow.runArrivalProcesses().getResultMessage();

        assertThat(result).contains("Apple arrival message.", "Zoo arrival message.");
        assertThat(result.indexOf("Apple arrival message.")).isLessThan(result.indexOf("Zoo arrival message."));
    }

    @Test
    void runArrivalProcesses_doesNotExecuteRegularProcesses() {
        addProcess(new GenericCommandDescription("regular"), "Regular process message.");
        addArrivalProcess(new GenericCommandDescription("arrival"), "Arrival process message.");

        String result = workflow.runArrivalProcesses().getResultMessage();

        assertThat(result).isEqualTo("Arrival process message.");
    }

    @Test
    void runProcesses_doesNotExecuteArrivalProcesses() {
        addProcess(new GenericCommandDescription("regular"), "Regular process message.");
        addArrivalProcess(new GenericCommandDescription("arrival"), "Arrival process message.");

        String result = workflow.runProcesses().getResultMessage();

        assertThat(result).isEqualTo("Regular process message.");
    }

    @Test
    void respondTo_triesEverySiblingSharingTheSameDescription_untilOneSucceeds() {
        // The premise the PrepositionCondition/AdverbCondition feature relies on: two authored
        // Response rows sharing the same verb+adjective+noun (e.g. "switch lamp" gated on
        // PREPOSITION on/off respectively) must both be reachable, not have the second silently
        // overwrite the first.
        GenericCommandDescription switchLamp = new GenericCommandDescription("switch", "lamp");
        Command failingSibling = mock(Command.class);
        when(failingSibling.execute()).thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE));
        Command succeedingSibling = mock(Command.class);
        when(succeedingSibling.execute())
                .thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS, "The lamp is now on."));

        workflow.addResponse(switchLamp, failingSibling);
        workflow.addResponse(switchLamp, succeedingSibling);

        ExecutionResult result = workflow.respondTo(switchLamp);

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("The lamp is now on.");
    }

    @Test
    void respondTo_whenNoSiblingSucceeds_surfacesTheLastFailuresMessage() {
        GenericCommandDescription switchLamp = new GenericCommandDescription("switch", "lamp");
        Command firstSibling = mock(Command.class);
        when(firstSibling.execute())
                .thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "Not that way."));
        Command lastSibling = mock(Command.class);
        when(lastSibling.execute())
                .thenReturn(new CommandExecutionResult(ExecutionResult.State.FAILURE, "Still not that way."));

        workflow.addResponse(switchLamp, firstSibling);
        workflow.addResponse(switchLamp, lastSibling);

        ExecutionResult result = workflow.respondTo(switchLamp);

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("Still not that way.");
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
