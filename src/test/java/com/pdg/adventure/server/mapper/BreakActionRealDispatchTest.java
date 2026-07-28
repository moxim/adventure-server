package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.BreakActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.BreakAction;
import com.pdg.adventure.server.annotation.AutoMapperRegistrationProcessor;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.mapper.action.BreakActionMapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Proves a BreakActionData inside a CommandData survives real CommandMapper.mapToBO dispatch -
 * i.e. that @Service + @AutoRegisterMapper actually registers BreakActionMapper with
 * MapperSupporter, rather than only working under the mocked MapperSupporter that
 * BreakActionMapperTest exercises. A missing registration here would surface as an NPE
 * (MapperSupporter.getMapper returns null) the moment an author saves a command with a Break
 * action and the adventure is reloaded - not caught by any unit test that mocks the registry.
 */
class BreakActionRealDispatchTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void breakActionData_dispatchesThroughTheRealMapperRegistry_insideACommand() {
        context = new AnnotationConfigApplicationContext();
        context.register(GameContext.class, AdventureConfig.class, MapperSupporter.class,
                         AutoMapperRegistrationProcessor.class, CommandDescriptionMapper.class,
                         CommandMapper.class, BreakActionMapper.class);
        context.refresh();

        CommandMapper commandMapper = context.getBean(CommandMapper.class);

        CommandData commandData = new CommandData(new CommandDescriptionData("jump||sea"));
        commandData.addAction(new BreakActionData());

        Command command = commandMapper.mapToBO(commandData);

        assertThat(command.getActions()).hasSize(1);
        assertThat(command.getActions().getFirst()).isInstanceOf(BreakAction.class);
        assertThat(command.getActions().getFirst().isBreak()).isTrue();
    }
}
