package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.server.action.DecrementVariableAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class DecrementVariableActionMapperTest {

    private static final String VAR_NAME = "score";
    private static final String VAR_VALUE = "5";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private VariableProvider variableProvider;
    @Mock private MessagesHolder messagesHolder;

    @InjectMocks private DecrementVariableActionMapper mapper;

    @Test
    void mapToBO_buildsActionWithNameAndValue() {
        DecrementVariableActionData data = new DecrementVariableActionData(VAR_NAME, VAR_VALUE);

        DecrementVariableAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(DecrementVariableAction.class);
        assertThat(result.getName()).isEqualTo(VAR_NAME);
        assertThat(result.getValue()).isEqualTo(VAR_VALUE);
    }

    @Test
    void mapToDO_roundTripsNameAndValue() {
        DecrementVariableAction action = new DecrementVariableAction(VAR_NAME, VAR_VALUE, variableProvider,
                                                                     messagesHolder);

        DecrementVariableActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull();
        assertThat(data.getName()).isEqualTo(VAR_NAME);
        assertThat(data.getValue()).isEqualTo(VAR_VALUE);
    }
}
