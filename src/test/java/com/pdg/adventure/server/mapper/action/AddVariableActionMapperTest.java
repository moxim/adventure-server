package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.AddVariableActionData;
import com.pdg.adventure.server.action.AddVariableAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class AddVariableActionMapperTest {

    private static final String FIRST = "bonus";
    private static final String SECOND = "score";

    @Mock private VariableProvider variableProvider;
    @Mock private MapperSupporter mapperSupporter;

    private AddVariableActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AddVariableActionMapper(mapperSupporter);
    }

    @Test
    void mapToBO_buildsActionWithBothNames() {
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        AddVariableActionData data = new AddVariableActionData();
        data.setFirstName(FIRST);
        data.setSecondName(SECOND);
        data.setId("id");

        AddVariableAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(AddVariableAction.class);
        assertThat(result.getFirstName()).isEqualTo(FIRST);
        assertThat(result.getSecondName()).isEqualTo(SECOND);
        assertThat(result.getId()).isEqualTo(data.getId());
    }

    @Test
    void mapToDO_roundTripsBothNames() {
        AddVariableAction action = new AddVariableAction(FIRST, SECOND, variableProvider);
        AddVariableActionData data = mapper.mapToDO(action);
        assertThat(data).isNotNull();
        assertThat(data.getFirstName()).isEqualTo(FIRST);
        assertThat(data.getSecondName()).isEqualTo(SECOND);
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
