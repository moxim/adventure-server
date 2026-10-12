package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.PrintVariableActionData;
import com.pdg.adventure.server.action.PrintVariableAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class PrintVariableActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;
    @Mock private GameContext gameContext;

    @InjectMocks private PrintVariableActionMapper mapper;

    @Test
    void mapToBO_buildsAnActionThatPrintsTheSharedVariableThroughTheGameContext() {
        VariableProvider variables = new VariableProvider();
        variables.set("VISITED", 14);
        when(mapperSupporter.getVariableProvider()).thenReturn(variables);
        PrintVariableActionData data = new PrintVariableActionData();
        data.setId("a1");
        data.setName("VISITED");

        PrintVariableAction result = mapper.mapToBO(data);
        result.execute();

        assertThat(result.getId()).isEqualTo("a1");
        assertThat(result.getName()).isEqualTo("VISITED");
        verify(gameContext).tell("VISITED : 14");
    }

    @Test
    void mapToDO_roundTripsIdAndName() {
        PrintVariableAction action = new PrintVariableAction("score", s -> { }, new VariableProvider());
        action.setId("a2");

        PrintVariableActionData result = mapper.mapToDO(action);

        assertThat(result.getId()).isEqualTo("a2");
        assertThat(result.getName()).isEqualTo("score");
    }
}
