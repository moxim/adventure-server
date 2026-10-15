package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.DiagnoseVariableActionData;
import com.pdg.adventure.server.action.DiagnoseVariableAction;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class DiagnoseVariableActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    @InjectMocks private DiagnoseVariableActionMapper mapper;

    @Test
    void mapToBO_buildsAnActionThatPrintsTheSharedVariable() {
        VariableProvider variables = new VariableProvider();
        variables.set("VISITED", 14);
        when(mapperSupporter.getVariableProvider()).thenReturn(variables);
        DiagnoseVariableActionData data = new DiagnoseVariableActionData();
        data.setId("a1");
        data.setName("VISITED");

        DiagnoseVariableAction result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("a1");
        assertThat(result.getName()).isEqualTo("VISITED");
        assertThat(result.execute().getResultMessage()).isEqualTo("VISITED : 14");
    }

    @Test
    void mapToDO_roundTripsIdAndName() {
        DiagnoseVariableAction action = new DiagnoseVariableAction("score", new VariableProvider());
        action.setId("a2");

        DiagnoseVariableActionData result = mapper.mapToDO(action);

        assertThat(result.getId()).isEqualTo("a2");
        assertThat(result.getName()).isEqualTo("score");
    }
}
