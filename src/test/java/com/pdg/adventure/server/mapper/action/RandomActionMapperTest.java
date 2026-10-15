package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.RandomActionData;
import com.pdg.adventure.server.action.RandomAction;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class RandomActionMapperTest {

    private static final String VAR_NAME = "score";

    @Mock private VariableProvider variableProvider;
    @Mock private MapperSupporter mapperSupporter;

    private RandomActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RandomActionMapper(mapperSupporter);
    }

    @Test
    void mapToBO_buildsActionWithName() {
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        RandomActionData data = new RandomActionData();
        data.setName(VAR_NAME);
        data.setId("id");

        RandomAction result = mapper.mapToBO(data);

        assertThat(result.getName()).isEqualTo(VAR_NAME);
        assertThat(result.getId()).isEqualTo("id");
    }

    @Test
    void mapToDO_roundTripsName() {
        RandomAction action = new RandomAction(VAR_NAME, variableProvider);

        RandomActionData data = mapper.mapToDO(action);

        assertThat(data.getName()).isEqualTo(VAR_NAME);
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
