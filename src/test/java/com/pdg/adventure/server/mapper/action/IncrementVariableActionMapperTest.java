package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.server.action.IncrementVariableAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class IncrementVariableActionMapperTest {

    private static final String VAR_NAME = "score";
    private static final String VAR_VALUE = "5";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private VariableProvider variableProvider;
    @Mock private MessagesHolder messagesHolder;

    @InjectMocks private IncrementVariableActionMapper mapper;

    @Test
    void mapToBO_buildsActionWithNameAndValue() {
        IncrementVariableActionData data = new IncrementVariableActionData();
        data.setName(VAR_NAME);
        data.setValue(VAR_VALUE);
        data.setId("id");

        IncrementVariableAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(IncrementVariableAction.class);
        assertThat(result.getName()).isEqualTo(VAR_NAME);
        assertThat(result.getValue()).isEqualTo(VAR_VALUE);
        assertThat(result.getId()).isEqualTo(data.getId());
    }

    @Test
    void mapToDO_roundTripsNameAndValue() {
        IncrementVariableAction action = new IncrementVariableAction(VAR_NAME, VAR_VALUE, variableProvider,
                                                                     messagesHolder);
        IncrementVariableActionData data = mapper.mapToDO(action);
        assertThat(data).isNotNull();
        assertThat(data.getName()).isEqualTo(VAR_NAME);
        assertThat(data.getValue()).isEqualTo(VAR_VALUE);
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
