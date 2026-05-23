package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.server.action.SetVariableAction;
import com.pdg.adventure.server.mapper.action.SetVariableActionMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class SetVariableActionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private MessagesHolder messagesHolder;

    @Mock
    private VariableProvider variableProvider;

    private SetVariableActionMapper setVariableActionMapper;

    @BeforeEach
    void setUp() {
        setVariableActionMapper = new SetVariableActionMapper(variableProvider, messagesHolder, mapperSupporter);
    }

    @Test
    void mapToDOAndBO() {
        // given
        SetVariableAction setVariableAction = new SetVariableAction("name", "value", variableProvider, messagesHolder);
        Mapper<SetVariableActionData, SetVariableAction> mapper = setVariableActionMapper;

        // when
        final SetVariableActionData setVariableActionData = mapper.mapToDO(setVariableAction);

        // then
        assertThat(setVariableActionData).isNotNull();
        assertThat(setVariableActionData).isInstanceOf(ActionData.class);
        assertThat(setVariableActionData.getActionName()).isEqualTo("SetVariableActionData");
        assertThat(setVariableActionData.getVariableName()).isEqualTo("name");
        assertThat(setVariableActionData.getVariableValue()).isEqualTo("value");

        // when
        final SetVariableAction setVariableActionReborn = mapper.mapToBO(setVariableActionData);

        // then
        assertThat(setVariableActionReborn).isNotNull();
        assertThat(setVariableActionReborn).isInstanceOf(SetVariableAction.class);
        assertThat(setVariableActionReborn.getVariableName()).isEqualTo("name");
        assertThat(setVariableActionReborn.getVariableValue()).isEqualTo("value");
    }
}
