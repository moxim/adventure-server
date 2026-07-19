package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.CommandData;
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
}
