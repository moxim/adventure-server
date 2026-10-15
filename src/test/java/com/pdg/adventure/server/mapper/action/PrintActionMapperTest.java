package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.PrintActionData;
import com.pdg.adventure.server.action.PrintAction;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class PrintActionMapperTest {

    private static final String VAR_NAME = "score";

    @Mock private VariableProvider variableProvider;
    @Mock private MapperSupporter mapperSupporter;

    private PrintActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PrintActionMapper(mapperSupporter);
    }

    @Test
    void mapToBO_buildsActionWithName() {
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        PrintActionData data = new PrintActionData();
        data.setName(VAR_NAME);
        data.setId("id");

        PrintAction result = mapper.mapToBO(data);

        assertThat(result.getName()).isEqualTo(VAR_NAME);
        assertThat(result.getId()).isEqualTo("id");
    }

    @Test
    void mapToDO_roundTripsName() {
        PrintAction action = new PrintAction(VAR_NAME, variableProvider);

        PrintActionData data = mapper.mapToDO(action);

        assertThat(data.getName()).isEqualTo(VAR_NAME);
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
