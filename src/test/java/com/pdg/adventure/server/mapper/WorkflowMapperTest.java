package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;

@ExtendWith(MockitoExtension.class)
class WorkflowMapperTest {

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private Command command;

    private WorkflowMapper workflowMapper;

    @BeforeEach
    void setUp() {
        workflowMapper = new WorkflowMapper(commandMapper);
    }

    @Test
    void populate_addsMappedCommandsAsWorkflowPreCommands_soPreProcessCommandsExecutesThem() {
        // Given: an authored CommandData in a WorkflowData
        CommandData commandData = new CommandData(new CommandDescriptionData("shiver||"));
        WorkflowData workflowData = new WorkflowData();
        workflowData.getCommands().add(commandData);

        GenericCommandDescription runtimeDescription = new GenericCommandDescription("shiver", "", "");
        when(commandMapper.mapToBO(commandData)).thenReturn(command);
        when(command.getDescription()).thenReturn(runtimeDescription);
        when(command.execute()).thenReturn(
                new CommandExecutionResult(ExecutionResult.State.SUCCESS, "The room grows cold."));

        GameContext gameContext = new GameContext();
        Workflow workflow = gameContext.setUpWorkflows();

        // When: populating the runtime workflow from the authored data
        workflowMapper.populate(workflowData, workflow);

        // Then: gameContext.preProcessCommands() - the exact call GameLoop.run() makes each turn
        // at GameLoop.java:36 - now executes the authored command.
        gameContext.preProcessCommands();

        verify(commandMapper).mapToBO(commandData);
        verify(command).execute();
    }

    @Test
    void populate_addsMappedCommandsAsWorkflowInterceptorCommands_soInterceptCommandsExecutesThem() {
        // Given: an authored CommandData in a WorkflowData's interceptor list
        CommandData commandData = new CommandData(new CommandDescriptionData("shiver||"));
        WorkflowData workflowData = new WorkflowData();
        workflowData.getInterceptorCommands().add(commandData);

        GenericCommandDescription runtimeDescription = new GenericCommandDescription("shiver", "", "");
        when(commandMapper.mapToBO(commandData)).thenReturn(command);
        when(command.getDescription()).thenReturn(runtimeDescription);
        when(command.execute()).thenReturn(
                new CommandExecutionResult(ExecutionResult.State.SUCCESS, "The room grows cold."));

        GameContext gameContext = new GameContext();
        Workflow workflow = gameContext.setUpWorkflows();

        // When: populating the runtime workflow from the authored data
        workflowMapper.populate(workflowData, workflow);

        // Then: gameContext.interceptCommands(...) - the exact call GameLoop.runOneCommandSucceeded()
        // makes each turn at GameLoop.java:92 - now finds and executes the authored command.
        ExecutionResult result = gameContext.interceptCommands(runtimeDescription);

        verify(commandMapper).mapToBO(commandData);
        verify(command).execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    // Workflow.interceptCommands() looks the runtime command up in a TreeMap keyed by
    // CommandDescription.compareTo(), i.e. by GenericCommandDescription.getDescription() string
    // equality - so an authored response only ever fires if CommandDescriptionMapper produces the
    // exact same verb/adjective/noun shape the Parser hands runOneCommandSucceeded() each turn. The
    // two tests above mock commandMapper entirely and so never exercise that mapping; this one uses
    // the real CommandDescriptionMapper + CommandMapper chain to prove a verb-only authored response
    // (adjective/noun left unset -> null Words) actually matches the Parser's verb + "" + "" shape
    // (see SimpleSentence's EMPTY_STRING defaults in Parser.java), not verb + null + null.
    @Test
    void populate_authoredVerbOnlyInterceptor_matchesParserStyleDescriptionAtRuntime() {
        CommandDescriptionMapper realDescriptionMapper = new CommandDescriptionMapper(null);
        CommandMapper realCommandMapper = new CommandMapper(null, realDescriptionMapper);
        WorkflowMapper realWorkflowMapper = new WorkflowMapper(realCommandMapper);

        CommandDescriptionData descriptionData =
                new CommandDescriptionData(new Word("shiver", Word.Type.VERB), null, null);
        CommandData commandData = new CommandData(descriptionData);
        WorkflowData workflowData = new WorkflowData();
        workflowData.getInterceptorCommands().add(commandData);

        GameContext gameContext = new GameContext();
        Workflow workflow = gameContext.setUpWorkflows();

        realWorkflowMapper.populate(workflowData, workflow);

        GenericCommandDescription parserStyleDescription = new GenericCommandDescription("shiver", "", "");
        ExecutionResult result = gameContext.interceptCommands(parserStyleDescription);

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
