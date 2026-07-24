package com.pdg.adventure.server.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.service.AdventureService;

@ExtendWith(MockitoExtension.class)
class LoadAdventureActionTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private AdventureMapper adventureMapper;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private Location startLocation;

    private GameContext gameContext;
    private LoadAdventureAction loadAdventureAction;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        lenient().when(adventureConfig.allMessages()).thenReturn(new MessagesHolder());
        lenient().when(adventureConfig.allLocations()).thenReturn(new HashMap<>());
        lenient().when(adventureConfig.allItems()).thenReturn(new HashMap<>());
        lenient().when(adventureConfig.allContainers()).thenReturn(new HashMap<>());
        loadAdventureAction = new LoadAdventureAction(adventureService, adventureMapper, adventureConfig, gameContext);
    }

    @Test
    void loadAdventure_populatesGameContextWorkflowData_fromAdventureDatasWorkflowData() {
        AdventureData adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setCurrentLocationId("loc-1");
        LocationData locationData = new LocationData();
        locationData.setId("loc-1");
        adventureData.getLocationData().put("loc-1", locationData);
        WorkflowData workflowData = new WorkflowData();
        workflowData.getCommands().add(new CommandData(new CommandDescriptionData("shiver||")));
        adventureData.setWorkflowData(workflowData);

        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));

        Adventure adventure = new Adventure(null, null, null, null);
        adventure.setCurrentLocationId("loc-1");
        when(startLocation.getId()).thenReturn("loc-1");
        adventure.setLocations(List.of(startLocation));

        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        assertThatThrownBy(() -> loadAdventureAction.loadAdventure("adv-1"))
                .isInstanceOf(ReloadAdventureException.class);

        assertThat(gameContext.getWorkflowData()).isSameAs(workflowData);
        assertThat(gameContext.getWorkflowData().getCommands()).hasSize(1);
    }
}
