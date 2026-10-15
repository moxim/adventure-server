package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.DiagnoseVariablesActionData;
import com.pdg.adventure.server.action.DiagnoseVariablesAction;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class DiagnoseVariablesActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    @InjectMocks private DiagnoseVariablesActionMapper mapper;

    @Test
    void mapToBO_buildsAnActionThatListsTheSharedVariables() {
        VariableProvider variables = new VariableProvider();
        variables.set("VISITED", 14);
        variables.set("TURNS", 3);
        when(mapperSupporter.getVariableProvider()).thenReturn(variables);
        DiagnoseVariablesActionData data = new DiagnoseVariablesActionData();
        data.setId("a1");

        DiagnoseVariablesAction result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("a1");
        assertThat(result.execute().getResultMessage()).isEqualTo("TURNS : 3\nVISITED : 14");
    }

    @Test
    void mapToDO_roundTripsTheId() {
        DiagnoseVariablesAction action = new DiagnoseVariablesAction(new VariableProvider());
        action.setId("a2");

        assertThat(mapper.mapToDO(action).getId()).isEqualTo("a2");
    }
}
