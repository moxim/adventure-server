package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.AutoDropActionData;
import com.pdg.adventure.model.action.AutoRemoveActionData;
import com.pdg.adventure.model.action.AutoTakeActionData;
import com.pdg.adventure.model.action.AutoWearActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.AutoDropAction;
import com.pdg.adventure.server.action.AutoRemoveAction;
import com.pdg.adventure.server.action.AutoTakeAction;
import com.pdg.adventure.server.action.AutoWearAction;
import com.pdg.adventure.server.annotation.AutoMapperRegistrationProcessor;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.mapper.action.AutoDropActionMapper;
import com.pdg.adventure.server.mapper.action.AutoRemoveActionMapper;
import com.pdg.adventure.server.mapper.action.AutoTakeActionMapper;
import com.pdg.adventure.server.mapper.action.AutoWearActionMapper;
import com.pdg.adventure.server.support.MapperSupporter;

/** Proves AutoTake/AutoDrop/AutoWear/AutoRemove survive real CommandMapper dispatch, i.e. their mappers are auto-registered. */
class AutoTakeDropRealDispatchTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void autoTakeAndAutoDropData_dispatchThroughTheRealMapperRegistry() {
        context = new AnnotationConfigApplicationContext();
        context.register(GameContext.class, AdventureConfig.class, MapperSupporter.class,
                         AutoMapperRegistrationProcessor.class, CommandDescriptionMapper.class,
                         CommandMapper.class, AutoTakeActionMapper.class, AutoDropActionMapper.class,
                         AutoWearActionMapper.class, AutoRemoveActionMapper.class);
        context.refresh();
        CommandMapper commandMapper = context.getBean(CommandMapper.class);

        CommandData take = new CommandData(new CommandDescriptionData("take||~"));
        take.addAction(new AutoTakeActionData());
        CommandData drop = new CommandData(new CommandDescriptionData("drop||~"));
        drop.addAction(new AutoDropActionData());

        CommandData wear = new CommandData(new CommandDescriptionData("wear||~"));
        wear.addAction(new AutoWearActionData());
        CommandData remove = new CommandData(new CommandDescriptionData("remove||~"));
        remove.addAction(new AutoRemoveActionData());

        Command mappedTake = commandMapper.mapToBO(take);
        Command mappedDrop = commandMapper.mapToBO(drop);

        assertThat(mappedTake.getActions()).singleElement().isInstanceOf(AutoTakeAction.class);
        assertThat(mappedDrop.getActions()).singleElement().isInstanceOf(AutoDropAction.class);
        assertThat(commandMapper.mapToBO(wear).getActions()).singleElement().isInstanceOf(AutoWearAction.class);
        assertThat(commandMapper.mapToBO(remove).getActions()).singleElement().isInstanceOf(AutoRemoveAction.class);
    }
}
