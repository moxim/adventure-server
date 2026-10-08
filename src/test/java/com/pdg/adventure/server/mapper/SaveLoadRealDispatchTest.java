package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.LoadGameActionData;
import com.pdg.adventure.model.action.SaveGameActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadGameAction;
import com.pdg.adventure.server.action.SaveGameAction;
import com.pdg.adventure.server.annotation.AutoMapperRegistrationProcessor;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.GameStateSnapshotter;
import com.pdg.adventure.server.mapper.action.LoadGameActionMapper;
import com.pdg.adventure.server.mapper.action.SaveGameActionMapper;
import com.pdg.adventure.server.storage.repository.SavedGameRepository;
import com.pdg.adventure.server.storage.service.SavedGameService;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.support.FakeSessionScopeConfig;

/** Proves SaveGame/LoadGame survive real CommandMapper dispatch, i.e. their mappers are auto-registered. */
class SaveLoadRealDispatchTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void saveAndLoadGameData_dispatchThroughTheRealMapperRegistry() {
        context = new AnnotationConfigApplicationContext();
        context.register(FakeSessionScopeConfig.class, GameContext.class, AdventureConfig.class,
                         MapperSupporter.class, AutoMapperRegistrationProcessor.class, CommandDescriptionMapper.class,
                         CommandMapper.class, GameStateSnapshotter.class, SaveGameActionMapper.class,
                         LoadGameActionMapper.class);
        context.registerBean(SavedGameService.class, () -> new SavedGameService(mock(SavedGameRepository.class)));
        context.refresh();
        CommandMapper commandMapper = context.getBean(CommandMapper.class);

        CommandData save = new CommandData(new CommandDescriptionData("save||~"));
        save.addAction(new SaveGameActionData());
        CommandData load = new CommandData(new CommandDescriptionData("load||~"));
        load.addAction(new LoadGameActionData());

        Command mappedSave = commandMapper.mapToBO(save);
        Command mappedLoad = commandMapper.mapToBO(load);

        assertThat(mappedSave.getActions()).singleElement().isInstanceOf(SaveGameAction.class);
        assertThat(mappedLoad.getActions()).singleElement().isInstanceOf(LoadGameAction.class);
    }
}
