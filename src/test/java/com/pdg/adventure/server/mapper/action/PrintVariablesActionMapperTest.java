package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.PrintVariablesActionData;
import com.pdg.adventure.server.action.PrintVariablesAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class PrintVariablesActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;
    @Mock private GameContext gameContext;

    @InjectMocks private PrintVariablesActionMapper mapper;

    @Test
    void mapToBO_buildsAnActionThatListsTheSharedVariablesThroughTheGameContext() {
        VariableProvider variables = new VariableProvider();
        variables.set("VISITED", 14);
        variables.set("TURNS", 3);
        when(mapperSupporter.getVariableProvider()).thenReturn(variables);
        PrintVariablesActionData data = new PrintVariablesActionData();
        data.setId("a1");

        PrintVariablesAction result = mapper.mapToBO(data);
        result.execute();

        assertThat(result.getId()).isEqualTo("a1");
        verify(gameContext).tell("TURNS : 3\nVISITED : 14");
    }

    @Test
    void mapToDO_roundTripsTheId() {
        PrintVariablesAction action = new PrintVariablesAction(s -> { }, new VariableProvider());
        action.setId("a2");

        assertThat(mapper.mapToDO(action).getId()).isEqualTo("a2");
    }
}
