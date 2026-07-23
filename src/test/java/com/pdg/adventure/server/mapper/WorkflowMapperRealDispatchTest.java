package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoMapperRegistrationProcessor;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.mapper.action.MessageActionMapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Exercises the full, real mapper-dispatch chain that WorkflowMapperTest and LoadAdventureActionTest
 * each mock away: authored WorkflowData -> real CommandMapper.mapToBO -> real
 * MapperSupporter.getMapper(MessageActionData.class) -> the auto-registered MessageActionMapper ->
 * a runtime Command executed by gameContext.preProcessCommands(), the exact call GameLoop.run() makes
 * at the top of every turn.
 */
class WorkflowMapperRealDispatchTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void authoredWorkflowCommand_executesThroughRealMapperRegistry_whenGameLoopPreProcessesCommands() {
        context = new AnnotationConfigApplicationContext();
        context.register(GameContext.class, AdventureConfig.class, MapperSupporter.class,
                         AutoMapperRegistrationProcessor.class, CommandDescriptionMapper.class,
                         CommandMapper.class, MessageActionMapper.class, WorkflowMapper.class);
        context.refresh();

        GameContext gameContext = context.getBean(GameContext.class);
        WorkflowMapper workflowMapper = context.getBean(WorkflowMapper.class);

        CommandData commandData = new CommandData(new CommandDescriptionData("shiver||"));
        MessageActionData messageAction = new MessageActionData();
        messageAction.setMessageId("THE_ROOM_GROWS_COLD");
        commandData.addAction(messageAction);

        WorkflowData workflowData = new WorkflowData();
        workflowData.getCommands().add(commandData);
        gameContext.setWorkflowData(workflowData);

        Workflow workflow = gameContext.setUpWorkflows();
        workflowMapper.populate(gameContext.getWorkflowData(), workflow);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
        try {
            gameContext.preProcessCommands();
        } finally {
            System.setOut(originalOut);
        }

        assertThat(capturedOut.toString()).contains("THE_ROOM_GROWS_COLD");
    }
}
